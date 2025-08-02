package memorius.aof;

import memorius.MemoryValue;
import memorius.resp.RESPReader;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AOFLoader {

    private Map<String, MemoryValue> dataStore;
    private static final String filePath = "appendonly.aof";

    public AOFLoader(Map<String, MemoryValue> dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Loads the data store from the append-only file and removes expired keys.
     */
    public void loadAOF() {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            RESPReader reader = new RESPReader(fis);

            while (true) {
                try {
                    Object obj = reader.read();
                    if (obj instanceof Object[] array && array.length > 0) {
                        String cmd = array[0].toString().toUpperCase();

                        switch (cmd) {
                            case "RECORD" -> {
                                if (array.length >= 3) {
                                    String key = array[1].toString();
                                    String val = array[2].toString();
                                    dataStore.put(key, new MemoryValue(MemoryValue.Type.STRING, val));
                                }
                            }
                            case "EXPIATE" -> {
                                if (array.length >= 3) {
                                    String key = array[1].toString();
                                    long expirationTime = Long.parseLong(array[2].toString());
                                    MemoryValue mv = dataStore.get(key);
                                    if (mv != null) {
                                        mv.setExpiration(expirationTime);
                                    }
                                }
                            }
                            case "PURGE" -> {
                                if (array.length >= 2) {
                                    for (int i = 1; i < array.length; i++) {
                                        String key = array[i].toString();
                                        dataStore.remove(key);
                                    }
                                }
                            }
                            case "FLUSH" -> {
                                dataStore.clear();
                            }
                            case "APPEND" -> {
                                if (array.length >= 3) {
                                    String key = array[1].toString();
                                    List<String> list = getOrCreateList(dataStore, key);
                                    // Add to head (LPUSH)
                                    list.add(0, array[2].toString());
                                }
                            }
                            case "EXTEND" -> {
                                if (array.length >= 3) {
                                    String key = array[1].toString();
                                    List<String> list = getOrCreateList(dataStore, key);
                                    for (int i = 2; i < array.length; i++) {
                                        list.add(array[i].toString());
                                    }
                                }
                            }
                            case "SUMMON" -> {
                                if (array.length >= 2) {
                                    String key = array[1].toString();
                                    MemoryValue mv = dataStore.get(key);
                                    if (mv != null && mv.getType() == MemoryValue.Type.LIST) {
                                        List<String> list = mv.asList();
                                        if (!list.isEmpty()) {
                                            list.remove(0);  // pop from left
                                        }
                                    }
                                }
                            }

                            default -> {
                            }
                        }
                    }
                } catch (EOFException e) {
                    break;
                }
            }

            long now = System.currentTimeMillis();
            dataStore.entrySet().removeIf(entry -> {
                MemoryValue mv = entry.getValue();
                Long exp = mv.getExpiration();
                return exp != null && exp > 0 && exp <= now;
            });

            System.out.println("[MEMORIUS] AOF restored and expired keys purged.");
        } catch (FileNotFoundException e) {
            System.out.println("[MEMORIUS] No AOF file found, starting fresh.");
        } catch (IOException e) {
            System.err.println("[MEMORIUS-ERROR] Failed to load AOF: " + e.getMessage());
        }
    }

    private static List<String> getOrCreateList(Map<String, MemoryValue> store, String key) {
        MemoryValue mv = store.get(key);
        if (mv == null || mv.getType() != MemoryValue.Type.LIST) {
            mv = new MemoryValue(MemoryValue.Type.LIST, new ArrayList<String>());
            store.put(key, mv);
        }
        return mv.asList();
    }
}
