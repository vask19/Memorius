package memorius.application;

import memorius.MemoryValue;
import memorius.aof.AOFLoader;
import memorius.aof.AOFLogger;
import memorius.command.CommandProcessor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Starter {

    private static final Map<String, MemoryValue> dataStore = new ConcurrentHashMap<>();
    private ExpirationDaemon expirationDaemon;
    private AOFLogger aofLogger;
    private AOFLoader aofLoader;
    private CommandProcessor commandProcessor;
    private MemoriusServer memoriusServer;

    public void start(int port) {
        System.out.println("[MEMORIUS-OMNISSIAH] Listening on port " + port);

        try {
            aofLoader = new AOFLoader(dataStore);
            aofLogger = new AOFLogger("appendonly.aof");
            commandProcessor = new CommandProcessor(dataStore, aofLogger);
            memoriusServer = new MemoriusServer(commandProcessor);

            expirationDaemon = new ExpirationDaemon(dataStore);
            expirationDaemon.start();

            aofLoader.loadAOF();
            memoriusServer.startListening(port);

        } catch (IOException e) {
            System.err.println("[MEMORIUS-ERROR] Startup failed: " + e.getMessage());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void shutdown() {
        try {
            if (aofLogger != null) aofLogger.close();
            if (expirationDaemon != null) expirationDaemon.stop();
            System.out.println("[MEMORIUS] Shutdown complete.");
        } catch (IOException e) {
            System.err.println("[MEMORIUS-ERROR] Failed during shutdown: " + e.getMessage());
        }
    }
}
