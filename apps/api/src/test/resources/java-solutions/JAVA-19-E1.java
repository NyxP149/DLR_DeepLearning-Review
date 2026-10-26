import java.util.*;

public class Main {
    static final Map<String, Integer> balances = new TreeMap<>();

    static String transfer(String from, String to, int amount) {
        Map<String, Integer> snapshot = new TreeMap<>(balances);
        balances.merge(from, -amount, Integer::sum);
        balances.merge(to, amount, Integer::sum);
        if (balances.get(from) < 0) {
            balances.clear();
            balances.putAll(snapshot);
            return "ROLLBACK";
        }
        return "COMMIT";
    }

    public static void main(String[] args) {
        balances.put("A", 100);
        balances.put("B", 50);
        int totalBefore = balances.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Avant : " + balances);
        String first = transfer("A", "B", 30);
        System.out.println("Transfert 30 A->B : " + first + " " + balances);
        String second = transfer("A", "B", 500);
        System.out.println("Transfert 500 A->B : " + second + " " + balances);
        int totalAfter = balances.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Total conserve : " + (totalAfter == totalBefore));
    }
}
