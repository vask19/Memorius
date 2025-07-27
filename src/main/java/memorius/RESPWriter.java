package memorius;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RESPWriter {
    private final OutputStream out;

    public RESPWriter(OutputStream out) {
        this.out = out;
    }

    public void writeSimpleString(String message) throws IOException {
        out.write(('+'
                + message
                + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    public void writeError(String message) throws IOException {
        out.write(('-'
                + message
                + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    public void writeInteger(long value) throws IOException {
        out.write((':' + String.valueOf(value) + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    public void writeBulkString(String value) throws IOException {
        if (value == null) {
            out.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.write(("$" + bytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(bytes);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    public void writeArray(List<String> elements) throws IOException {
        if (elements == null) {
            out.write("*-1\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        out.write(('*' + String.valueOf(elements.size()) + "\r\n").getBytes(StandardCharsets.UTF_8));
        for (String element : elements) {
            writeBulkString(element);
        }
    }

    public void writeArrayRaw(Object[] elements) throws IOException {
        if (elements == null) {
            out.write("*-1\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        out.write(("*" + elements.length + "\r\n").getBytes(StandardCharsets.UTF_8));

        for (Object element : elements) {
            if (element == null) {
                out.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                String str = element.toString();
                byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                out.write(("$" + bytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
                out.write(bytes);
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
