# ⚙️ MEMORIUS

> Engine of Thought. Java Cogitator Archive.

**Memorius** is a lightweight in-memory key-value store written in pure Java.  
It supports basic string and list operations, key expiration, and append-only persistence.

![memorius_logo.png](logo/memorius_logo.png)
---

## 🧬 Features

- Key-value string storage
- List-type operations (push, pop, count, range)
- Key expiration (TTL)
- Append-only file (AOF) persistence
- RESP protocol support (Redis protocol-compatible)
- Simple command set

---

## 📜 Commands
| Command     | Description                                           |
|-------------|-------------------------------------------------------|
| `OMNISSIAH` | Check server connectivity (like a ping)               |
| `CHANT`     | Echo back the provided message                        |
| `RECORD`    | Store a value under a key                             |
| `RETRIEVE`  | Get the value stored under a key                      |
| `PURGE`     | Delete one or more keys                               |
| `EXPIATE`   | Set an expiration time (in seconds) for a key         |
| `LIFESPAN`  | Get remaining time to live for a key (in seconds)     |
| `FLUSH`     | Clear all keys from memory                            |
| `EXISTS`    | Check if specified keys exist                         |
| `TYPE`      | Get the type of value stored at a key                 |
| `KEYS`      | Return a list of all active keys                      |
| `APPEND`    | Prepend a value to a list stored at a key             |
| `EXTEND`    | Add multiple values to a list                         |
| `SUMMON`    | Remove and return the first element of a list         |
| `COUNT`     | Return the number of items in a list                  |
| `INVOKE`    | Return all elements of a list                         |

---

## 🔧 Build and Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build and start server
```bash
mvn clean install
java -jar target/memorius.jar
