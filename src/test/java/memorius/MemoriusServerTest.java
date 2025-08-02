package memorius;

import memorius.aof.AOFLogger;
import memorius.command.CommandProcessor;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class MemoriusServerTest {

    private CommandProcessor processor;

    @BeforeEach
    public void setUp() {
        Map<String, MemoryValue> testDB = new ConcurrentHashMap<>();
        processor = new CommandProcessor(testDB, new AOFLoggerMock());
    }

    @Test
    public void testPing() throws IOException {
        Object response = processor.execute(new Object[]{"PING"});
        assertEquals("PONG", response);
    }

    @Test
    public void testEcho() throws IOException {
        Object response = processor.execute(new Object[]{"ECHO", "Hello"});
        assertEquals("Hello", response);
    }

    @Test
    public void testSetAndGet() throws IOException {
        processor.execute(new Object[]{"SET", "foo", "bar"});
        Object response = processor.execute(new Object[]{"GET", "foo"});
        assertEquals("bar", response);
    }

    @Test
    public void testUnknownCommand() throws IOException {
        Object response = processor.execute(new Object[]{"FOOBAR"});
        assertTrue(response.toString().contains("unknown command"));
    }

    static class AOFLoggerMock extends AOFLogger {
        public AOFLoggerMock() {
            super();
        }

        @Override
        public void append(Object[] command) {
        }
    }

}
