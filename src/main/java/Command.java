import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

interface Command {
    static byte[] compress(byte[] content) {
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

    static byte[] withHeader(byte[] content, String type) {
        ByteArrayBuilder bout = new ByteArrayBuilder();
        bout.append((type + " " + content.length + "\0").getBytes());
        bout.append(content);
        return bout.toByteArray();
    }

    static String getFilePath(String hash) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        FileSystem.createPath(".git/objects/" + folder);
        return ".git/objects/" + folder + "/" + filename;
    }

    static Hash storeInTree(File file, String type) {
        byte[] content = FileSystem.readBytes(file);
        return storeInTree(content, type);
    }

    static Hash storeInTree(byte[] content, String type) {
        byte[] bytes = withHeader(content, type);
        Hash hashed = new Hash(bytes);
        FileSystem.writeFile(getFilePath(hashed.asString), compress(bytes));
        System.out.println("Created " + type + " " + hashed.asString);
        return hashed;
    }

    void execute();

    class Init implements Command {
        private final FileSystem fileSystem;

        public Init(FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }

        public void execute() {
            // Exercise: Error if we are already inside a git repository
            fileSystem.createPath(".git/objects");
            fileSystem.createPath(".git/refs/heads");
            fileSystem.writeFile(".git/HEAD", "ref: refs/heads/master");
        }
    }

    class Commit implements Command {
        public void execute() {
            File[] files = FileSystem.listFiles(".");
            ByteArrayBuilder tree = new ByteArrayBuilder();
            for (File file : files) {
                Hash hash = Command.storeInTree(file, "blob");
                tree.append(("100644 " + file.getName() + "\0").getBytes());
                tree.append(hash.asBytes);
            }
            Hash treeHash = Command.storeInTree(tree.toByteArray(), "tree");
            // Exercise: Add parent commit
            // Exercise: Add correct timestamp
            // Exercise: Add commit message
            // Exercise: Add configurable committer and author
            byte[] commit = ("tree " + treeHash.asString + "\n" +
                    "author CCL <ccl@praqma.net> 1\n" +
                    "committer CCL <ccl@praqma.net> 1").getBytes();
            Hash commitHash = Command.storeInTree(commit, "commit");
            FileSystem.writeFile(".git/refs/heads/master", commitHash.asString);
        }
    }

    class NullCommand implements Command {
        public void execute() {
            System.out.println("Invalid or missing argument.");
            // Exercise: Print help
        }
    }
}
