import java.util.*;
import java.util.stream.*;

public class Main {
    record Booking(String guest, int nights, int rate, String status) {
        Booking {
            if (nights <= 0) throw new IllegalArgumentException("nuits invalides");
        }

        int amount() {
            return nights * rate;
        }
    }

    public static void main(String[] args) {
        List<Booking> bookings = new ArrayList<>(List.of(
                new Booking("Ada", 2, 150, "CONFIRMED"),
                new Booking("Nyx", 1, 120, "CONFIRMED"),
                new Booking("Linus", 1, 60, "CONFIRMED"),
                new Booking("Ada", 3, 100, "CANCELLED")));
        List<Booking> confirmed = bookings.stream().filter(b -> b.status().equals("CONFIRMED")).toList();
        Map<String, Integer> revenue = confirmed.stream()
                .collect(Collectors.groupingBy(Booking::guest, Collectors.summingInt(Booking::amount)));
        List<Map.Entry<String, Integer>> ranking = new ArrayList<>(revenue.entrySet());
        ranking.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        System.out.println("Reservations: " + confirmed.size());
        System.out.println("Revenu: " + confirmed.stream().mapToInt(Booking::amount).sum() + " EUR");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : ranking) {
            System.out.println(rank++ + ". " + entry.getKey() + " : " + entry.getValue());
        }
        try {
            new Booking("Bob", 0, 90, "CONFIRMED");
        } catch (IllegalArgumentException exception) {
            System.out.println("Rejetee : " + exception.getMessage());
        }
    }
}
