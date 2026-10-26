import java.util.*;

public class Main {
    static <T extends Comparable<T>> T maximum(List<T> items) {
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) > 0) best = item;
        }
        return best;
    }

    static double total(List<? extends Number> numbers) {
        double sum = 0;
        for (Number number : numbers) sum += number.doubleValue();
        return sum;
    }

    record Pair<A, B>(A first, B second) {}

    public static void main(String[] args) {
        List<Integer> ages = List.of(31, 18, 45, 27);
        List<String> names = List.of("Nyx", "Ada", "Linus");
        List<Double> prices = List.of(19.9, 4.5, 24.9);
        System.out.println("Plus grand age : " + maximum(ages));
        System.out.println("Dernier nom : " + maximum(names));
        System.out.println("Total prix : " + String.format(Locale.ROOT, "%.1f", total(prices)));
        Pair<String, Integer> pair = new Pair<>(maximum(names), maximum(ages));
        System.out.println("Paire : " + pair);
    }
}
