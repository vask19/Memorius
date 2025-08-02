package memorius.command;

public enum Ritual {
    OMNISSIAH,      // PING
    CHANT,          // ECHO
    RECORD,         // SET
    RETRIEVE,       // GET
    PURGE,          // DEL
    EXISTS,         // EXISTS
    EXPIATE,        // EXPIRE
    LIFESPAN,       // TTL
    FLUSH,          // FLUSHALL
    APPEND,         // For lists
    EXTEND,         // For appending multiple
    SUMMON,         // LPOP / RPOP
    COUNT,          // LLEN
    TYPE,           // TYPE key
    KEYS,           // KEYS *
    INVOKE,
    UNKNOWN;

    public static Ritual from(String name) {
        try {
            return Ritual.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
