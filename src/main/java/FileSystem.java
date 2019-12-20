import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.DeflaterOutputStream;

class FileSystem {
    private final Path root;

    public FileSystem(Path root) {
        this.root = root;
    }

    void createPath(String path) {
        new File(path).mkdirs();
    }

    static File[] listFiles(String path) {
        // Exercise: Add support .gitignore
        // Exercise: Add support for folders
        return new File(path).listFiles(File::isFile);
    }

    static byte[] readBytes(File path) {
        try {
            return Files.readAllBytes(path.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException();
        }
    }

    void writeFile(String path, String content) {
        try {
            Files.write(Paths.get(path), content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException();
        }
    }

    static void writeFile(String path, byte[] content) {
        try {
            Files.write(Paths.get(path), content);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException();
        }
    }

    public String getFilePath(String hash) {
        String folder = hash.substring(0, 2);
        String filename = hash.substring(2);
        createPath(".git/objects/" + folder);
        return ".git/objects/" + folder + "/" + filename;
    }

    public static byte[] compress(byte[] content) {
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

    public static byte[] withHeader(byte[] content, String type) {
        ByteArrayBuilder bout = new ByteArrayBuilder();
        bout.append((type + " " + content.length + "\0").getBytes());
        bout.append(content);
        return bout.toByteArray();
    }

    public Hash storeInTree(byte[] content, String type) {
        byte[] bytes = withHeader(content, type);
        Hash hashed = new Hash(bytes);
        writeFile(getFilePath(hashed.asString), compress(bytes));
        System.out.println("Created " + type + " " + hashed.asString);
        return hashed;
    }

    public Hash storeInTree(File file, String type) {
        byte[] content = readBytes(file);
        return storeInTree(content, type);
    }
}
