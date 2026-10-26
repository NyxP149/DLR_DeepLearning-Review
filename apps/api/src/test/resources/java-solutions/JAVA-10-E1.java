import java.util.*;
import java.util.stream.Collectors;

public class Main {
    record Product(String sku, String name, double price) {
        @Override public boolean equals(Object other) {
            return other instanceof Product product && product.sku.equals(sku);
        }

        @Override public int hashCode() {
            return sku.hashCode();
        }
    }

    public static void main(String[] args) {
        List<Product> lines = List.of(
                new Product("K-1", "Clavier", 49.9),
                new Product("S-2", "Souris", 19.9),
                new Product("K-1", "Clavier promo", 39.9),
                new Product("C-3", "Cable", 4.5),
                new Product("S-2", "Souris sans fil", 24.9),
                new Product("M-4", "Ecran", 149.0),
                new Product("B-5", "Bloc", 19.9));
        Set<Product> unique = new HashSet<>(lines);
        List<Product> sorted = new ArrayList<>(unique);
        sorted.sort(Comparator.comparingDouble(Product::price).reversed().thenComparing(Product::name));
        String text = sorted.stream().map(p -> p.name() + " (" + p.price() + ")").collect(Collectors.joining(", "));
        System.out.println("Produits : " + text);
        System.out.println("Doublons ignores : " + (lines.size() - unique.size()));
    }
}
