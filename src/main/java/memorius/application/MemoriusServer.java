package memorius.application;

import memorius.command.CommandProcessor;
import memorius.resp.RESPReader;
import memorius.resp.RESPWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class MemoriusServer {

    private CommandProcessor commandProcessor;

    public MemoriusServer(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    protected void startListening(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[MEMORIUS] New client connected: " + clientSocket);
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleClient(Socket clientSocket) {
        try (clientSocket;
             InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {
            try {
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
                        } else if (response instanceof List<?> list) {
                            writer.writeArray((List<String>) list);
                        } else {
                            writer.writeBulkString(response.toString());
                        }
                    } else {
                        writer.writeError("ERR unknown or invalid command format");
                    }
                }

            } catch (IOException e) {
                System.err.println("[MEMORIUS-ERROR] Client connection error: " + e.getMessage());
            }
        } catch (IOException ignored) {
        }
    }
}
