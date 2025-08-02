package memorius.application;

import memorius.MemoryValue;

import java.util.Map;
import java.util.concurrent.*;

public class ExpirationDaemon {

    private final Map<String, MemoryValue> database;
    private final ScheduledExecutorService scheduler;

    public ExpirationDaemon(Map<String, MemoryValue> database) {
        this.database = database;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, MemoryValue> entry : database.entrySet()) {
                MemoryValue value = entry.getValue();
                if (value.isExpired()) {
                    database.remove(entry.getKey());
                    System.out.println("[DAEMON] Purged expired key: " + entry.getKey());
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
