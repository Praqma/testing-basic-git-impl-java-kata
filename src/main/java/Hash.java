import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Hash {
    final byte[] asBytes;
    final String asString;

    Hash(byte[] content) {
        asBytes = hash(content);
        asString = toHexString(asBytes);
    }

    private byte[] hash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(content);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException();
        }
    }

    static String toHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
