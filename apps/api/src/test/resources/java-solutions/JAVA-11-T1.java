import java.util.*;

public class Main {
    static <T extends Comparable<T>> T min(List<T> items) {
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) < 0) best = item;
        }
        return best;
    }

    static double total(Collection<? extends Number> numbers) {
        double sum = 0;
        for (Number number : numbers) sum += number.doubleValue();
        return sum;
    }

    static void fill(List<? super Integer> target, int count) {
        for (int i = 0; i < count; i++) target.add(i);
    }

    public static void main(String[] args) {
        List<String> names = List.of("Nyx", "Ada", "Linus");
        List<Integer> scores = List.of(40, 12, 33);
        List<Number> numbers = List.of(1, 2.5, 4L);
        List<Object> filled = new ArrayList<>();
        System.out.println("Plus petit nom : " + min(names));
        System.out.println("Plus petit score : " + min(scores));
        System.out.println("Somme : " + total(numbers));
        fill(filled, 3);
        System.out.println("Liste remplie : " + filled);
    }
}
