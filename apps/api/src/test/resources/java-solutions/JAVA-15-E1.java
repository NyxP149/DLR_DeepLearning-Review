import java.util.*;

public class Main {
    interface Notifier { void send(String message); }

    static class FakeNotifier implements Notifier {
        final List<String> sent = new ArrayList<>();
        public void send(String message) { sent.add(message); }
    }

    static class OrderService {
        private final Notifier notifier;
        OrderService(Notifier notifier) { this.notifier = notifier; }

        String place(int amount) {
            if (amount <= 0) throw new IllegalArgumentException("montant invalide");
            if (amount >= 100) notifier.send("gros montant " + amount);
            return "OK";
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
        FakeNotifier fake = new FakeNotifier();
        OrderService service = new OrderService(fake);
        check("gros montant notifie", service.place(150).equals("OK") && fake.sent.size() == 1);

        fake = new FakeNotifier();
        service = new OrderService(fake);
        check("petit montant silencieux", service.place(50).equals("OK") && fake.sent.isEmpty());

        boolean refused = false;
        try {
            new OrderService(new FakeNotifier()).place(0);
        } catch (IllegalArgumentException e) {
            refused = true;
        }
        check("montant nul refuse", refused);

        fake = new FakeNotifier();
        service = new OrderService(fake);
        service.place(100);
        boolean notifiedAtLimit = fake.sent.size() == 1;
        service.place(99);
        check("limite 100 notifie", notifiedAtLimit && fake.sent.size() == 1);

        System.out.println("Resultat : " + passed + "/" + total);
    }
}
