import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

record Product(long id, String name, int price) {}

record Page<T>(List<T> content, int number, int totalPages, int totalElements) {}

public class Main {
    static Page<Product> findAll(List<Product> all, int page, int size) {
        var sorted = new ArrayList<>(all);
        sorted.sort(Comparator.comparingInt(Product::price));
        int totalPages = (sorted.size() + size - 1) / size;
        int from = Math.min(page * size, sorted.size());
        int to = Math.min(from + size, sorted.size());
        return new Page<>(sorted.subList(from, to), page, totalPages, sorted.size());
    }

    public static void main(String[] args) {
        var products = List.of(
            new Product(1, "Souris", 20), new Product(2, "Clavier", 50), new Product(3, "Câble", 10),
            new Product(4, "Écran", 200), new Product(5, "Webcam", 80));
        for (int number = 0; number < 4; number++) {
            var page = findAll(products, number, 2);
            var names = new ArrayList<String>();
            for (var product : page.content()) {
                names.add(product.name());
            }
            System.out.println("Page " + (number + 1) + "/" + page.totalPages() + ": " + (names.isEmpty() ? "(vide)" : String.join(", ", names)));
        }
    }
}
