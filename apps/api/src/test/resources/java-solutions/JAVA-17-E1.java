interface Discount { int apply(int total); }
class TenPercent implements Discount { public int apply(int total) { return total / 10; } }
public class Main {
    public static void main(String[] args) {
        Discount discount = new TenPercent();
        System.out.println("Remise: " + discount.apply(150));
    }
}
