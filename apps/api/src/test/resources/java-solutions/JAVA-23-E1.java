import java.util.List;
record Order(String customer, int amount, boolean paid) {
    Order { if (amount <= 0) throw new IllegalArgumentException("amount"); }
}
public class Main {
    public static void main(String[] args) {
        var orders = List.of(new Order("Ada", 120, true), new Order("Linus", 80, false), new Order("Grace", 30, true));
        var paid = orders.stream().filter(Order::paid).toList();
        int revenue = paid.stream().mapToInt(Order::amount).sum();
        if (paid.size() != 2 || revenue != 150) throw new AssertionError();
        System.out.println("Commandes: " + paid.size());
        System.out.println("Revenu: " + revenue + " EUR");
    }
}
