import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("journal.txt");
        List<String> lines = List.of(
                "2026-08-30;Java;45",
                "2026-08-31;SQL;30",
                "2026-09-01;Révision;60",
                "2026-09-03;Docker;20");
        Files.write(path, lines, StandardCharsets.UTF_8);
        List<String> read = Files.readAllLines(path, StandardCharsets.UTF_8);
        Map<YearMonth, Integer> byMonth = new TreeMap<>();
        for (String line : read) {
            String[] fields = line.split(";");
            YearMonth month = YearMonth.from(LocalDate.parse(fields[0]));
            byMonth.merge(month, Integer.parseInt(fields[2]), Integer::sum);
        }
        byMonth.forEach((month, minutes) -> System.out.println(month + " : " + minutes + " min"));
        System.out.println("Jour : " + LocalDate.parse(read.get(0).split(";")[0]).getDayOfWeek());
        System.out.println("Accent conserve : " + read.get(2).contains("Révision"));
    }
}
