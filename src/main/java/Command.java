import java.io.File;

interface Command {

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
        private final FileSystem fileSystem;

        public Commit(FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }

        public void execute() {
            File[] files = fileSystem.listFiles(".");
            ByteArrayBuilder tree = new ByteArrayBuilder();
            for (File file : files) {
                Hash hash = fileSystem.storeInTree(file, "blob");
                tree.append(("100644 " + file.getName() + "\0").getBytes());
                tree.append(hash.asBytes);
            }
            Hash treeHash = fileSystem.storeInTree(tree.toByteArray(), "tree");
            // Exercise: Add parent commit
            // Exercise: Add correct timestamp
            // Exercise: Add commit message
            // Exercise: Add configurable committer and author
            byte[] commit = ("tree " + treeHash.asString + "\n" +
                    "author CCL <ccl@praqma.net> 1\n" +
                    "committer CCL <ccl@praqma.net> 1").getBytes();
            Hash commitHash = fileSystem.storeInTree(commit, "commit");
            fileSystem.writeFile(".git/refs/heads/master", commitHash.asString);
        }
    }

    class NullCommand implements Command {
        public void execute() {
            System.out.println("Invalid or missing argument.");
            // Exercise: Print help
        }
    }
}
