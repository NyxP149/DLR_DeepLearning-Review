import java.util.*;

public class Main {
    static final Map<String, Integer> stock = new LinkedHashMap<>();
    static final Set<String> processed = new HashSet<>();

    static String reserve(String key, Map<String, Integer> lines) {
        if (!processed.add(key)) return "ignore";
        Map<String, Integer> snapshot = new LinkedHashMap<>(stock);
        try {
            for (Map.Entry<String, Integer> line : lines.entrySet()) {
                int left = stock.get(line.getKey()) - line.getValue();
                if (left < 0) throw new IllegalStateException("stock insuffisant " + line.getKey());
                stock.put(line.getKey(), left);
            }
            return "OK";
        } catch (IllegalStateException exception) {
            stock.clear();
            stock.putAll(snapshot);
            return "ECHEC " + exception.getMessage() + ", rollback";
        }
    }

    public static void main(String[] args) {
        stock.put("pomme", 10);
        stock.put("poire", 3);
        Map<String, Integer> first = new LinkedHashMap<>();
        first.put("pomme", 3);
        first.put("poire", 1);
        Map<String, Integer> second = new LinkedHashMap<>();
        second.put("pomme", 2);
        second.put("poire", 5);
        System.out.println("K1: " + reserve("K1", first) + " " + stock);
        System.out.println("K1 rejoue: " + reserve("K1", first) + " " + stock);
        System.out.println("K2: " + reserve("K2", second) + " " + stock);
    }
}
