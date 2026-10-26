import java.util.*;

public class Main {
    static final Map<String, Integer> stock = new LinkedHashMap<>();

    static String reserve(Map<String, Integer> order) {
        Map<String, Integer> snapshot = new LinkedHashMap<>(stock);
        boolean valid = true;
        for (Map.Entry<String, Integer> line : order.entrySet()) {
            int left = stock.merge(line.getKey(), -line.getValue(), Integer::sum);
            if (left < 0) valid = false;
        }
        if (!valid) {
            stock.clear();
            stock.putAll(snapshot);
            return "ROLLBACK";
        }
        return "COMMIT";
    }

    public static void main(String[] args) {
        stock.put("pomme", 5);
        stock.put("poire", 2);
        System.out.println("Avant : " + stock);
        System.out.println("Commande 1 : " + reserve(new LinkedHashMap<>(Map.of("pomme", 2, "poire", 1))) + " " + stock);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("pomme", 2);
        second.put("poire", 5);
        System.out.println("Commande 2 : " + reserve(second) + " " + stock);
        System.out.println("Stock jamais negatif : " + stock.values().stream().allMatch(v -> v >= 0));
    }
}
