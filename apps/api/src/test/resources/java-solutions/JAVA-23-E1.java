import java.util.*;
import java.util.stream.Collectors;

public class Main {
    record Order(String customer, int amount, boolean paid) {
        Order {
            if (amount <= 0) throw new IllegalArgumentException("montant invalide");
        }
    }

    static String report(List<Order> orders) {
        List<Order> paid = orders.stream().filter(Order::paid).toList();
        int revenue = paid.stream().mapToInt(Order::amount).sum();
        Map<String, Integer> byCustomer = paid.stream()
                .collect(Collectors.groupingBy(Order::customer, Collectors.summingInt(Order::amount)));
        List<Map.Entry<String, Integer>> ranking = byCustomer.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .toList();
        StringBuilder text = new StringBuilder();
        text.append("Commandes: ").append(paid.size()).append('\n');
        text.append("Revenu: ").append(revenue).append(" EUR");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : ranking) {
            text.append('\n').append(rank++).append(". ").append(entry.getKey()).append(" : ").append(entry.getValue());
        }
        return text.toString();
    }

    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("Ada", 120, true),
                new Order("Linus", 30, true),
                new Order("Grace", 70, true),
                new Order("Linus", 80, false),
                new Order("Ada", 80, true));
        System.out.println(report(orders));
        try {
            new Order("Bug", 0, true);
        } catch (IllegalArgumentException e) {
            System.out.println("Rejetee : " + e.getMessage());
        }
    }
}
