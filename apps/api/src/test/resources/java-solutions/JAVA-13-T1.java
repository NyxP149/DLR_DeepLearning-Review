import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        List<String> lines = List.of("2026-02-27;Soutenance;90", "2026-03-01;Révision;30", "2026-03-15;Atelier;60");
        Path file = Path.of("agenda.txt");
        Files.write(file, lines, StandardCharsets.UTF_8);
        List<String> read = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<LocalDate> dates = new ArrayList<>();
        for (String line : read) dates.add(LocalDate.parse(line.split(";")[0]));
        LocalDate first = dates.get(0);
        LocalDate last = dates.get(dates.size() - 1);
        System.out.println("Premiere : " + first);
        System.out.println("Derniere : " + last);
        System.out.println("Ecart : " + ChronoUnit.DAYS.between(first, last) + " jours");
        System.out.println("Jour de la derniere : " + last.getDayOfWeek());
        System.out.println("Accent conserve : " + read.get(1).contains("Révision"));
    }
}
