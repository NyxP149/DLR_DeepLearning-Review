import java.util.*;
import java.util.stream.Collectors;

public class Main {
    record Order(String customer, double amount, String status) {}

    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("Nyx", 50.0, "PAID"),
                new Order("Ada", 35.0, "PAID"),
                new Order("Linus", 120.0, "PAID"),
                new Order("Nyx", 30.0, "PAID"),
                new Order("Ada", 80.0, "PENDING"),
                new Order("Linus", 15.0, "CANCELLED"));
        double paid = orders.stream().filter(o -> o.status().equals("PAID")).mapToDouble(Order::amount).sum();
        System.out.println("Total paye : " + paid);
        String customers = orders.stream().filter(o -> o.status().equals("PAID"))
                .map(Order::customer).distinct().sorted().collect(Collectors.joining(", "));
        System.out.println("Clients : " + customers);
        Map<String, Long> byStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::status, TreeMap::new, Collectors.counting()));
        System.out.println("Par statut : " + byStatus);
        Order biggest = orders.stream().max(Comparator.comparingDouble(Order::amount)).orElseThrow();
        System.out.println("Plus grosse commande : " + biggest.customer() + " (" + biggest.amount() + ")");
    }
}
