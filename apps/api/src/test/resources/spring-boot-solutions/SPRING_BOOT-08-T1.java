public class Main {
    interface RateClient {
        double rate(String destination);
    }

    static class ShippingService {
        private final RateClient client;

        ShippingService(RateClient client) {
            this.client = client;
        }

        double cost(String destination, int parcels) {
            double rate;
            try {
                rate = client.rate(destination);
            } catch (RuntimeException exception) {
                rate = 9.9;
            }
            return rate * parcels;
        }
    }

    static int passed = 0;
    static int total = 0;

    static void check(String name, boolean ok) {
        total++;
        if (ok) passed++;
        System.out.println((ok ? "PASS " : "FAIL ") + name);
    }

    public static void main(String[] args) {
        RateClient fake = destination -> 5.0;
        RateClient failing = destination -> {
            throw new RuntimeException("reseau coupe");
        };
        check("cout avec tarif du client", new ShippingService(fake).cost("FR", 3) == 15.0);
        check("repli quand le client echoue", new ShippingService(failing).cost("FR", 2) == 19.8);
        System.out.println("Resultat: " + passed + "/" + total + " reussis");
    }
}
