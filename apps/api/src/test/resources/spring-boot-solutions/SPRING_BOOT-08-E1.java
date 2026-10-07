interface TaxClient {
    int ratePercent();
}

class PriceService {
    private final TaxClient client;

    PriceService(TaxClient client) {
        this.client = client;
    }

    int withTax(int net) {
        int rate;
        try {
            rate = client.ratePercent();
        } catch (IllegalStateException error) {
            rate = 0;
        }
        return net + net * rate / 100;
    }
}

public class Main {
    static int passed = 0;
    static int total = 0;

    static void check(String name, int expected, int actual) {
        total++;
        if (expected == actual) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            System.out.println("FAIL " + name + " (attendu " + expected + ", obtenu " + actual + ")");
        }
    }

    public static void main(String[] args) {
        TaxClient fake = () -> 20;
        TaxClient broken = () -> {
            throw new IllegalStateException("service taxe indisponible");
        };

        check("prix avec taxe 20%", 120, new PriceService(fake).withTax(100));
        check("repli sans taxe", 100, new PriceService(broken).withTax(100));

        System.out.println("Résultat: " + passed + "/" + total + " réussis");
    }
}
