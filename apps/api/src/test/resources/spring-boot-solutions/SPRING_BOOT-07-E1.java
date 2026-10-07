import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class Bank {
    private final Map<String, Integer> balances = new TreeMap<>();
    private final Set<String> processed = new HashSet<>();

    Bank(Map<String, Integer> initial) {
        balances.putAll(initial);
    }

    String transfer(String key, String from, String to, int amount) {
        if (processed.contains(key)) {
            return "ignoré";
        }
        var snapshot = new TreeMap<>(balances);
        try {
            balances.merge(to, amount, Integer::sum);
            if (balances.get(from) < amount) {
                throw new IllegalStateException("fonds insuffisants");
            }
            balances.merge(from, -amount, Integer::sum);
            processed.add(key);
            return "OK";
        } catch (IllegalStateException error) {
            balances.clear();
            balances.putAll(snapshot);
            return "ÉCHEC " + error.getMessage() + ", rollback";
        }
    }

    String state() {
        return "A=" + balances.get("A") + " B=" + balances.get("B");
    }
}

public class Main {
    public static void main(String[] args) {
        var bank = new Bank(Map.of("A", 100, "B", 0));
        System.out.println("T1: " + bank.transfer("T1", "A", "B", 30) + " " + bank.state());
        System.out.println("T1 rejoué: " + bank.transfer("T1", "A", "B", 30) + " " + bank.state());
        System.out.println("T2: " + bank.transfer("T2", "A", "B", 500) + " " + bank.state());
    }
}
