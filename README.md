# ⚙️ MEMORIUS

> 🔴 Engine of Thought. ⚫ Java Cogitator Archive. ⚙️ Inspired by the Omnissiah.

**Memorius** is a lightweight key-value in-memory store written in pure Java. Designed in the grim darkness of the 41st millennium, it mimics the behavior of Redis but is themed entirely around the Machine God's archives.

![memorius_logo.png](logo/memorius_logo.png)
---

## 🧬 Features

| Ritual | Command           | Description                                  |
|--------|-------------------|----------------------------------------------|
| RECORD | `RECORD key val`  | Store a sacred value under a key             |
| RETRIEVE | `RETRIEVE key`  | Access memory recorded in the cogitator      |
| PURGE  | `PURGE key`       | Eliminate corrupted or outdated entries      |
| EXPIATE | `EXPIATE key sec`| Mark a key to expire after X seconds         |
| LIFESPAN | `LIFESPAN key`  | Reveal remaining life of the machine entry   |

---

## 🔧 Build and Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Run server:
```bash
mvn clean install
java -jar target/memorius.jar
