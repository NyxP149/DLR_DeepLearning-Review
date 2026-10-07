class Invoice {
    int total(int... amounts) {
        int total = 0; for (int amount : amounts) total += amount; return total;
    }
}
public class Main {
    public static void main(String[] args) {
        System.out.println("Total: " + new Invoice().total(100, 50));
    }
}
