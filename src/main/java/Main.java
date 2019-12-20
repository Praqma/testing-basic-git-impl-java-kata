import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;

public class Main {

    static class Hash {
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

    private static byte[] compress(byte[] content) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            OutputStream out = new DeflaterOutputStream(bout);
            out.write(content, 0, content.length);
            out.close();
            return bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException();
        }
    }

    private static byte[] withHeader(byte[] content, String type) {
        ByteArrayBuilder bout = new ByteArrayBuilder();
        bout.append((type + " " + content.length + "\0").getBytes());
        bout.append(content);
        return bout.toByteArray();
    }

    private static String getFilePath(String hash) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        FileSystem.createPath(".git/objects/" + folder);
        return ".git/objects/" + folder + "/" + filename;
    }

    private static Hash storeInTree(File file, String type) {
        byte[] content = FileSystem.readBytes(file);
        return storeInTree(content, type);
    }

    private static Hash storeInTree(byte[] content, String type) {
        byte[] bytes = withHeader(content, type);
        Hash hashed = new Hash(bytes);
        FileSystem.writeFile(getFilePath(hashed.asString), compress(bytes));
        System.out.println("Created " + type + " " + hashed.asString);
        return hashed;
    }

    interface Command {
        void execute();
    }

    static class Init implements Command {
        public void execute() {
            // Exercise: Error if we are already inside a git repository
            FileSystem.createPath(".git/objects");
            FileSystem.createPath(".git/refs/heads");
            FileSystem.writeFile(".git/HEAD", "ref: refs/heads/master");
        }
    }

    static class Commit implements Command {
        public void execute() {
            File[] files = FileSystem.listFiles("");
            ByteArrayBuilder tree = new ByteArrayBuilder();
            for (File file : files) {
                Hash hash = storeInTree(file, "blob");
                tree.append(("100644 " + file.getName() + "\0").getBytes());
                tree.append(hash.asBytes);
            }
            Hash treeHash = storeInTree(tree.toByteArray(), "tree");
            // Exercise: Add parent commit
            // Exercise: Add correct timestamp
            // Exercise: Add commit message
            // Exercise: Add configurable committer and author
            byte[] commit = ("tree " + treeHash.asString + "\n" +
                    "author CCL <ccl@praqma.net> 1\n" +
                    "committer CCL <ccl@praqma.net> 1").getBytes();
            Hash commitHash = storeInTree(commit, "commit");
            FileSystem.writeFile(".git/refs/heads/master", commitHash.asString);
        }
    }

    static class NullCommand implements Command {
        public void execute() {
            System.out.println("Invalid or missing argument.");
            // Exercise: Print help
        }
    }

    private static Command parseCommand(String[] args) {
        if (args.length == 0) return new NullCommand();
        else if ("init".equals((args[0]))) return new Init();
        else if ("commit".equals((args[0]))) return new Commit();
            // Exercise: Implement "help"
            // Exercise: Implement "log"
            // Exercise: Implement "hash-object"
            // Exercise: Implement "branches"
            // Exercise: Implement "(simple)tags"
            // Exercise: Implement "cat-file"
        else return new NullCommand();
    }

    public static void main(String[] args) {
        Command cmd = parseCommand(args);
        cmd.execute();
    }
}
