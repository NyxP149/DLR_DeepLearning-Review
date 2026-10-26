import java.util.List;

public class Main {
    interface Discount {
        String label();
        double apply(double price);
    }

    record Percent(int rate) implements Discount {
        public String label() { return "Percent " + rate; }
        public double apply(double price) { return price - price * rate / 100.0; }
    }

    record Fixed(double amount) implements Discount {
        public String label() { return "Fixed " + (int) amount; }
        public double apply(double price) { return price - amount; }
    }

    record None() implements Discount {
        public String label() { return "None"; }
        public double apply(double price) { return price; }
    }

    public static void main(String[] args) {
        double price = 100.0;
        double best = price;
        for (Discount discount : List.of(new Percent(10), new Fixed(5), new None())) {
            double result = discount.apply(price);
            System.out.println(discount.label() + " : " + result);
            best = Math.min(best, result);
        }
        System.out.println("Meilleur prix : " + best);
    }
}
