package memorius.command;

import memorius.MemoryValue;
import memorius.aof.AOFLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandProcessor {

    private final Map<String, MemoryValue> database;
    private final AOFLogger aofLogger;

    public CommandProcessor(Map<String, MemoryValue> database, AOFLogger aofLogger) {
        this.database = database;
        this.aofLogger = aofLogger;
    }

    public Object execute(Object[] array) throws IOException {
        if (array.length == 0) return "ERR empty ritual";

        String cmd = array[0].toString().toUpperCase();

        Ritual ritual = Ritual.from(cmd);
        return switch (ritual) {
            case OMNISSIAH -> "PRAISE-BE";

            case CHANT -> array.length < 2
                    ? "ERR insufficient devotion in 'chant'"
                    : array[1];

            case RECORD -> {
                if (array.length < 3)
                    yield "ERR corrupted ritual format for 'record'";
                String key = array[1].toString();
                String val = array[2].toString();
                MemoryValue value = new MemoryValue(MemoryValue.Type.STRING, val);
                database.put(key, value);
                aofLogger.append(array);
                yield "AFFIRMATIVE";
            }

            case RETRIEVE -> {
                if (array.length < 2)
                    yield "ERR retrieval incantation incomplete";
                String key = array[1].toString();
                MemoryValue value = database.get(key);
                if (value == null || value.isExpired()) {
                    database.remove(key);
                    yield null;
                }
                if (value.getType() != MemoryValue.Type.STRING) {
                    yield "ERR incompatible cogitator format";
                }
                yield value.asString();
            }

            case PURGE -> {
                if (array.length < 2)
                    yield "ERR purge requires targets";
                int count = 0;
                for (int i = 1; i < array.length; i++) {
                    String key = array[i].toString();
                    MemoryValue value = database.get(key);
                    if (value != null && !value.isExpired()) {
                        database.remove(key);
                        count++;
                    }
                }
                if (count > 0) {
                    aofLogger.append(array);
                }
                yield String.valueOf(count);
            }

            case EXPIATE -> {
                if (array.length < 3)
                    yield "ERR devotion requires time parameter";
                String key = array[1].toString();
                long seconds;
                try {
                    seconds = Long.parseLong(array[2].toString());
                } catch (NumberFormatException e) {
                    yield "ERR invalid number of seconds";
                }

                MemoryValue value = database.get(key);
                if (value == null || value.isExpired()) {
                    database.remove(key);
                    yield "ERR key not found or expired";
                }
                long expirationTime = System.currentTimeMillis() + seconds * 1000;
                value.setExpiration(expirationTime);

                Object[] aofCommand = new Object[] { "EXPIATE", key, String.valueOf(expirationTime) };
                aofLogger.append(aofCommand);

                yield "AFFIRMATIVE";
            }

            case LIFESPAN -> {
                if (array.length < 2)
                    yield "ERR must name memory to inspect";
                String key = array[1].toString();
                MemoryValue value = database.get(key);
                if (value == null || value.isExpired()) {
                    database.remove(key);
                    yield "-2"; // Redis-compatible: not found
                } else if (value.getExpiration() == null) {
                    yield "-1"; // Redis-compatible: no expiration
                } else {
                    long ttl = value.getExpiration() - System.currentTimeMillis();
                    yield String.valueOf(ttl / 1000);
                }
            }

            case EXISTS -> {
                if (array.length < 2)
                    yield "ERR specify memory traces to inspect";
                int existsCount = 0;
                for (int i = 1; i < array.length; i++) {
                    String key = array[i].toString();
                    MemoryValue value = database.get(key);
                    if (value != null && !value.isExpired()) {
                        existsCount++;
                    } else {
                        database.remove(key);
                    }
                }
                yield String.valueOf(existsCount);
            }

            case FLUSH -> {
                database.clear();
                aofLogger.append(array);
                yield "MEMORY PURGED";
            }

            case TYPE -> {
                if (array.length < 2)
                    yield "ERR specify memory to probe";
                String key = array[1].toString();
                MemoryValue value = database.get(key);
                if (value == null || value.isExpired()) {
                    database.remove(key);
                    yield "none";
                }
                yield value.getType().name().toLowerCase();
            }

            case KEYS -> {
                if (array.length < 2)
                    yield "ERR pattern required";
                String pattern = array[1].toString();
                if (!pattern.equals("*")) {
                    yield "ERR only '*' pattern is supported for now";
                }

                List<String> keys = new ArrayList<>();
                for (Map.Entry<String, MemoryValue> entry : database.entrySet()) {
                    MemoryValue value = entry.getValue();
                    if (value != null && !value.isExpired()) {
                        keys.add(entry.getKey());
                    } else {
                        database.remove(entry.getKey());
                    }
                }

                yield keys; // RESPWriter will handle it as writeArray
            }

            case APPEND -> {
                if (array.length < 3)
                    yield "ERR list append requires key and value";
                String key = array[1].toString();
                String value = array[2].toString();

                MemoryValue mv = database.get(key);
                List<String> list;

                if (mv == null) {
                    list = new ArrayList<>();
                    database.put(key, new MemoryValue(MemoryValue.Type.LIST, list));
                } else if (mv.getType() == MemoryValue.Type.LIST) {
                    list = mv.asList();
                } else {
                    yield "ERR incompatible memory type";
                }

                list.add(0, value); //LPUSH
                aofLogger.append(array);
                yield String.valueOf(list.size());
            }

            case EXTEND -> {
                if (array.length < 3)
                    yield "ERR extend requires key and values";
                String key = array[1].toString();
                MemoryValue mv = database.get(key);
                List<String> list;

                if (mv == null) {
                    list = new ArrayList<>();
                    database.put(key, new MemoryValue(MemoryValue.Type.LIST, list));
                } else if (mv.getType() == MemoryValue.Type.LIST) {
                    list = mv.asList();
                } else {
                    yield "ERR incompatible memory type";
                }

                for (int i = 2; i < array.length; i++) {
                    list.add(array[i].toString());
                }
                aofLogger.append(array);
                yield String.valueOf(list.size());
            }

            case SUMMON -> {
                if (array.length < 2)
                    yield "ERR summon requires key";
                String key = array[1].toString();
                MemoryValue mv = database.get(key);

                if (mv == null || mv.isExpired()) {
                    database.remove(key);
                    yield null;
                }

                if (mv.getType() != MemoryValue.Type.LIST) {
                    yield "ERR not a list";
                }

                List<String> list = mv.asList();
                if (list.isEmpty()) {
                    yield null;
                }

                String val = list.remove(0); // pop from left
                aofLogger.append(array);
                yield val;
            }

            case COUNT -> {
                if (array.length < 2)
                    yield "ERR count requires key";
                String key = array[1].toString();
                MemoryValue mv = database.get(key);

                if (mv == null || mv.isExpired()) {
                    database.remove(key);
                    yield "0";
                }

                if (mv.getType() != MemoryValue.Type.LIST) {
                    yield "ERR not a list";
                }

                yield String.valueOf(mv.asList().size());
            }

            case INVOKE -> {
                // Return all elements of the list (like LRANGE key 0 -1 in Redis)
                if (array.length < 2)
                    yield "ERR invoke requires key";
                String key = array[1].toString();
                MemoryValue mv = database.get(key);

                if (mv == null || mv.isExpired()) {
                    database.remove(key);
                    yield null;
                }

                if (mv.getType() != MemoryValue.Type.LIST) {
                    yield "ERR not a list";
                }

                List<String> list = mv.asList();
                yield new ArrayList<>(list);
            }

            //remove element from list
            case BANISH -> {
                if (array.length < 3)
                    yield "ERR banish requires key and value";
                String key = array[1].toString();
                String value = array[2].toString();

                MemoryValue mv = database.get(key);
                if (mv == null || mv.isExpired()) {
                    database.remove(key);
                    yield "0";
                }

                if (mv.getType() != MemoryValue.Type.LIST) {
                    yield "ERR not a list";
                }

                List<String> list = mv.asList();
                int before = list.size();
                list.removeIf(v -> v.equals(value));
                int removed = before - list.size();

                if (removed > 0) {
                    aofLogger.append(array);
                }
                yield String.valueOf(removed);
            }

            default -> "ERR unknown ritual '" + cmd.toLowerCase() + "'";
        };
    }
}
