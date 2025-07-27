package memorius;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoriusServer {

    private static final Map<String, String> database = new ConcurrentHashMap<>();
    private static AOFLogger aofLogger;
    private static CommandProcessor commandProcessor;

    public static void main(String[] args) {
        int port = 6379;
        System.out.println("[MEMORIUS-OMNISSIAH] Listening on port " + port);

        try {
            try (FileInputStream fis = new FileInputStream("appendonly.aof")) {
                RESPReader reader = new RESPReader(fis);
                while (true) {
                    try {
                        Object obj = reader.read();
                        if (obj instanceof Object[] array && array.length > 0) {
                            String cmd = array[0].toString().toUpperCase();
                            if ("SET".equals(cmd) && array.length >= 3) {
                                String key = array[1].toString();
                                String value = array[2].toString();
                                database.put(key, value);
                            }
                        }
                    } catch (EOFException e) {
                        break;
                    }
                }
                System.out.println("[MEMORIUS] AOF restored");
            } catch (FileNotFoundException e) {
                System.out.println("[MEMORIUS] No AOF file found, starting fresh.");
            }

            aofLogger = new AOFLogger("appendonly.aof");
            commandProcessor = new CommandProcessor(database, aofLogger);

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[MEMORIUS] New client connected: " + clientSocket);

                    new Thread(() -> handleClient(clientSocket)).start();
                }
            }

        } catch (IOException e) {
            System.err.println("[MEMORIUS-ERROR] Failed to start server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream()
        ) {
            RESPReader reader = new RESPReader(in);
            RESPWriter writer = new RESPWriter(out);

            while (!clientSocket.isClosed()) {
                Object command = reader.read();

                if (command instanceof Object[] array && array.length > 0) {
                    Object response = commandProcessor.execute(array);

                    if (response instanceof String str) {
                        if (str.startsWith("ERR ")) {
                            writer.writeError(str);
                        } else {
                            writer.writeBulkString(str);
                        }
                    } else if (response == null) {
                        writer.writeBulkString(null);
                    } else {
                        writer.writeBulkString(response.toString());
                    }

                } else if (command instanceof String strCommand && strCommand.equalsIgnoreCase("ping")) {
                    writer.writeSimpleString("PONG");
                } else {
                    writer.writeError("ERR unknown or invalid command format");
                }
            }

        } catch (IOException e) {
            System.err.println("[MEMORIUS-ERROR] Client connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

}
