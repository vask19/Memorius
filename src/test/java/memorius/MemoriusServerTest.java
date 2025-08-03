package memorius;

import memorius.aof.AOFLogger;
import memorius.command.CommandProcessor;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;
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
    public void testOmnissiah() throws IOException {
        Object response = processor.execute(new Object[]{"OMNISSIAH"});
        assertEquals("PRAISE-BE", response);
    }

    @Test
    public void testChant() throws IOException {
        Object response = processor.execute(new Object[]{"CHANT", "GLORY-TO-THE-MACHINE"});
        assertEquals("GLORY-TO-THE-MACHINE", response);
    }

    @Test
    public void testRecordAndRetrieve() throws IOException {
        processor.execute(new Object[]{"RECORD", "foo", "bar"});
        Object response = processor.execute(new Object[]{"RETRIEVE", "foo"});
        assertEquals("bar", response);
    }

    @Test
    public void testRetrieveNonexistent() throws IOException {
        Object response = processor.execute(new Object[]{"RETRIEVE", "nonexistent"});
        assertNull(response);
    }

    @Test
    public void testUnknownRitual() throws IOException {
        Object response = processor.execute(new Object[]{"FOOBAR"});
        assertTrue(response.toString().contains("unknown ritual"));
    }

    static class AOFLoggerMock extends AOFLogger {
        public AOFLoggerMock() {
            super();
        }

        @Override
        public void append(Object[] command) {
            // do nothing for testing
        }
    }

    @Test
    public void testExpiateAndLifespan() throws IOException, InterruptedException {
        processor.execute(new Object[]{"RECORD", "temp", "value"});
        processor.execute(new Object[]{"EXPIATE", "temp", "2"});

        Object ttlBefore = processor.execute(new Object[]{"LIFESPAN", "temp"});
        assertTrue(Integer.parseInt(ttlBefore.toString()) <= 2);

        Thread.sleep(2500);

        Object expired = processor.execute(new Object[]{"RETRIEVE", "temp"});
        assertNull(expired);
    }

    @Test
    public void testPurge() throws IOException {
        processor.execute(new Object[]{"RECORD", "key1", "val1"});
        processor.execute(new Object[]{"RECORD", "key2", "val2"});
        Object result = processor.execute(new Object[]{"PURGE", "key1", "key2"});
        assertEquals("2", result);
    }

    @Test
    public void testFlush() throws IOException {
        processor.execute(new Object[]{"RECORD", "one", "1"});
        processor.execute(new Object[]{"RECORD", "two", "2"});
        Object flushResult = processor.execute(new Object[]{"FLUSH"});
        assertEquals("MEMORY PURGED", flushResult);
        assertNull(processor.execute(new Object[]{"RETRIEVE", "one"}));
        assertNull(processor.execute(new Object[]{"RETRIEVE", "two"}));
    }

    @Test
    public void testAppendAndSummon() throws IOException {
        processor.execute(new Object[]{"APPEND", "list1", "val1"});
        processor.execute(new Object[]{"APPEND", "list1", "val2"});
        Object count = processor.execute(new Object[]{"COUNT", "list1"});
        assertEquals("2", count);

        Object first = processor.execute(new Object[]{"SUMMON", "list1"});
        assertEquals("val2", first);

        Object second = processor.execute(new Object[]{"SUMMON", "list1"});
        assertEquals("val1", second);
    }

    @Test
    public void testExtendAndInvoke() throws IOException {
        processor.execute(new Object[]{"EXTEND", "mylist", "a", "b", "c"});
        Object all = processor.execute(new Object[]{"INVOKE", "mylist"});

        assertInstanceOf(List.class, all);
        assertEquals(3, ((java.util.List<?>) all).size());
        assertEquals("a", ((java.util.List<?>) all).get(0));
    }

    @Test
    public void testExists() throws IOException {
        processor.execute(new Object[]{"RECORD", "k1", "v1"});
        processor.execute(new Object[]{"RECORD", "k2", "v2"});
        Object exists = processor.execute(new Object[]{"EXISTS", "k1", "k2", "missing"});
        assertEquals("2", exists);
    }

    @Test
    public void testType() throws IOException {
        processor.execute(new Object[]{"RECORD", "strKey", "value"});
        processor.execute(new Object[]{"APPEND", "listKey", "v"});

        Object strType = processor.execute(new Object[]{"TYPE", "strKey"});
        Object listType = processor.execute(new Object[]{"TYPE", "listKey"});

        assertEquals("string", strType);
        assertEquals("list", listType);
    }

    @Test
    public void testKeysWithAsteriskOnly() throws IOException {
        processor.execute(new Object[]{"RECORD", "a", "1"});
        processor.execute(new Object[]{"RECORD", "b", "2"});
        Object result = processor.execute(new Object[]{"KEYS", "*"});

        assertInstanceOf(List.class, result);
        assertTrue(((java.util.List<?>) result).contains("a"));
        assertTrue(((java.util.List<?>) result).contains("b"));
    }

    @Test
    public void testKeysInvalidPattern() throws IOException {
        Object response = processor.execute(new Object[]{"KEYS", "prefix*"});
        assertTrue(response.toString().contains("ERR only '*' pattern"));
    }

}
