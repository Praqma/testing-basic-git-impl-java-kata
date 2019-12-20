import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.InflaterOutputStream;

class SystemTests {

    static class Utils {
        static String readFile(String path) throws IOException {
            FileReader fr = new FileReader(path);
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = fr.read()) >= 0)
                sb.append((char) c);
            fr.close();
            return sb.toString();
        }

        static byte[] readCompressed(String path) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            OutputStream out = new InflaterOutputStream(bout);
            out.write(Files.readAllBytes(Paths.get(path)));
            out.close();
            return bout.toByteArray();
        }

        static String getFilePath(String hash) {
            String folder = hash.substring(0, 2);
            String filename = hash.substring(2);
            return ".git/objects/" + folder + "/" + filename;
        }

        static int indexOf(byte[] haystack, byte needle) {
            return indexOf(haystack, needle, 0);
        }

        static int indexOf(byte[] haystack, byte needle, int offset) {
            for (int i = offset; i < haystack.length; i++)
                if (haystack[i] == needle)
                    return i;
            return -1;
        }

        static void deleteFolder(String path) {
            deleteFolder(new File(path));
        }

        private static void deleteFolder(File file) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteFolder(f);
                }
            }
            file.delete();
        }
    }

    @BeforeAll
    static void preTests() {
        if (new File(".git").exists()) {
            System.out.println(".git folder already exists");
            System.exit(-1);
        }
    }

    @Test
    void init_complete() throws IOException {
        Main.main(new String[]{"init"});
        assertTrue(new File(".git").isDirectory(), ".git folder does not exist");
        assertTrue(new File(".git/objects").isDirectory(), "objects folder does not exist");
        assertTrue(new File(".git/refs").isDirectory(), "refs folder does not exist");
        assertTrue(new File(".git/refs/heads").isDirectory(), "ref/heads folder does not exist");
        assertTrue(new File(".git/HEAD").isFile(), "HEAD file does not exist");
        assertEquals("ref: refs/heads/master", Utils.readFile(".git/HEAD"), "HEAD file is incorrect");
    }

    byte[] checkObject(String hash, String type) throws IOException {
        assertEquals(40, hash.length(), type + " hash is invalid");
        assertTrue(new File(Utils.getFilePath(hash)).isFile(), type + " object does not exist");
        byte[] bytes = Utils.readCompressed(Utils.getFilePath(hash));
        int headerIndex = Utils.indexOf(bytes, (byte) 0) + 1;
        String header = new String(Arrays.copyOfRange(bytes, 0, headerIndex));
        assertEquals(type + " " + (bytes.length - headerIndex) + "\0", header, "invalid header in " + type + " object");
        return Arrays.copyOfRange(bytes, headerIndex, bytes.length);
    }

    @Test
    void commit_complete() throws IOException {
        Main.main(new String[]{"commit"});
        assertTrue(new File(".git/refs/heads/master").isFile(), "refs/heads/master file does not exist");
        String commitHash = Utils.readFile(".git/refs/heads/master");
        String commitContent = new String(checkObject(commitHash, "commit"));
        String treeHash = commitContent.substring("tree ".length(), commitContent.indexOf("\n"));
        assertTrue(commitContent.startsWith("tree"), "commit does not appear to contain a tree");
        byte[] treeContent = checkObject(treeHash, "tree");
        int pos = 0;
        while (pos < treeContent.length) {
            int spaceIndex = Utils.indexOf(treeContent, (byte) ' ', pos);
            String mode = new String(Arrays.copyOfRange(treeContent, pos, spaceIndex));
            int nullIndex = Utils.indexOf(treeContent, (byte) 0, spaceIndex);
            String filename = new String(Arrays.copyOfRange(treeContent, spaceIndex + 1, nullIndex));
            String hash = Hash.toHexString(Arrays.copyOfRange(treeContent, nullIndex + 1, nullIndex + 1 + 20));
            assertEquals("100644", mode, "not a blob");
            String blobContent = new String(checkObject(hash, "blob"));
            assertEquals(Utils.readFile(filename), blobContent, "file content does not match");
            pos = nullIndex + 20 + 1;
        }
    }

    @AfterAll
    static void postTests() {
        Utils.deleteFolder(".git");
    }
}
