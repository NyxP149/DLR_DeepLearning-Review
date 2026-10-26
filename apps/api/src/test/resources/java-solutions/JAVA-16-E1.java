import java.util.*;

public class Main {
    interface TaxPolicy { int tax(int amount); }

    record Invoice(List<Integer> lines) {
        int subtotal() {
            return lines.stream().mapToInt(Integer::intValue).sum();
        }

        int total(TaxPolicy policy) {
            return subtotal() + policy.tax(subtotal());
        }
    }

    static class InvoicePrinter {
        String render(Invoice invoice, TaxPolicy policy) {
            return "Facture: " + invoice.lines().size() + " lignes, total " + invoice.total(policy);
        }
    }

    public static void main(String[] args) {
        List<Integer> lines = List.of(100, 50);
        Invoice invoice = new Invoice(lines);
        TaxPolicy france = amount -> amount * 20 / 100;
        TaxPolicy exonere = amount -> 0;
        TaxPolicy reduit = amount -> amount * 10 / 100;
        System.out.println("Sous-total : " + invoice.subtotal());
        System.out.println("Total FR (20%) : " + invoice.total(france));
        System.out.println("Total exonere : " + invoice.total(exonere));
        System.out.println("Total reduit (10%) : " + invoice.total(reduit));
        System.out.println(new InvoicePrinter().render(invoice, france));
    }
}
