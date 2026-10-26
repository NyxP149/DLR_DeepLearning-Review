import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        int quantity = 3;
        int unitPrice = 14;
        int total = quantity * unitPrice;
        System.out.println("total=" + total);
        double avecTva = total * 1.2;
        System.out.println("avecTVA=" + String.format(Locale.ROOT, "%.2f", avecTva));
        int tronque = (int) avecTva;
        System.out.println("tronque=" + tronque);
        System.out.println("partEntiere=" + (total / 4));
        System.out.println("partExacte=" + (total / 4.0));
    }
}
