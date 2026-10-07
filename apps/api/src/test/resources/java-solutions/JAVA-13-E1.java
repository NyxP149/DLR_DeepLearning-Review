import java.nio.file.*;
import java.nio.charset.StandardCharsets;
public class Main {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("journal.txt");
        Files.writeString(path, "2026-08-30 | DLR", StandardCharsets.UTF_8);
        System.out.println(Files.readString(path, StandardCharsets.UTF_8));
    }
}
