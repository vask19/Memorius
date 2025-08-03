package memorius;

import java.io.Serializable;
import java.util.*;

public class MemoryValue implements Serializable {

    public enum Type {
        STRING,
        LIST,
        SET,
        HASH,
        ZSET
    }

    private final Type type;
    private final Object value;
    private Long expiration;

    public MemoryValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(long timestampMillis) {
        this.expiration = timestampMillis;
    }

    public boolean isExpired() {
        return expiration != null && System.currentTimeMillis() >= expiration;
    }

    public String asString() {
        return (String) value;
    }

    @SuppressWarnings("unchecked")
    public List<String> asList() {
        return (List<String>) value;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> asHash() {
        return (Map<String, String>) value;
    }

    @SuppressWarnings("unchecked")
    public Set<String> asSet() {
        return (Set<String>) value;
    }
}
