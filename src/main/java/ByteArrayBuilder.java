import java.util.LinkedList;
import java.util.List;

class ByteArrayBuilder {
    private List<byte[]> buffer;
    private int size;

    ByteArrayBuilder() {
        buffer = new LinkedList<>();
    }

    void append(byte[] arr) {
        buffer.add(arr);
        size += arr.length;
    }

    byte[] toByteArray() {
        byte[] result = new byte[size];
        int i = 0;
        for (byte[] arr : buffer)
            for (byte b : arr)
                result[i++] = b;
        return result;
    }
}
