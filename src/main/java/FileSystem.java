import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileSystem {
    static void createPath(String path) {
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

    static void writeFile(String path, String content) {
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
}
