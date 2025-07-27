package memorius;

import java.io.IOException;
import java.util.Map;

public class CommandProcessor {

    private final Map<String, String> database;
    private final AOFLogger aofLogger;

    public CommandProcessor(Map<String, String> database, AOFLogger aofLogger) {
        this.database = database;
        this.aofLogger = aofLogger;
    }

    public Object execute(Object[] array) throws IOException {
        if (array.length == 0) return "ERR empty command";

        String cmd = array[0].toString().toUpperCase();

        return switch (cmd) {
            case "PING" -> "PONG";
            case "ECHO" -> array.length < 2 ? "ERR wrong number of arguments for 'echo'" : array[1];
            case "SET" -> {
                if (array.length < 3) yield "ERR wrong number of arguments for 'set'";
                aofLogger.append(array);
                database.put(array[1].toString(), array[2].toString());
                yield "OK";
            }
            case "GET" -> {
                if (array.length < 2) yield "ERR wrong number of arguments for 'get'";
                yield database.get(array[1].toString());
            }
            default -> "ERR unknown command '" + cmd.toLowerCase() + "'";
        };
    }
}
