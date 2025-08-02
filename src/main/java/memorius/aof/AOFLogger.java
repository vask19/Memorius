package memorius.aof;

import memorius.resp.RESPWriter;

import java.io.*;

public class AOFLogger {
    private FileOutputStream fileOutput;

    public AOFLogger(String filename) throws IOException {
        this.fileOutput = new FileOutputStream(filename, true);
    }

    public AOFLogger() {
    }

    public synchronized void append(Object[] command) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        RESPWriter writer = new RESPWriter(buffer);
        writer.writeArrayRaw(command);
        fileOutput.write(buffer.toByteArray());
        fileOutput.flush();
    }

    public void close() throws IOException {
        fileOutput.close();
    }
}
