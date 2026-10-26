public class Main {
    enum Status {
        DRAFT, SENT, PAID;

        Status next() {
            if (this == PAID) throw new IllegalStateException("plus de transition depuis PAID");
            return values()[ordinal() + 1];
        }
    }

    static void transition(int id, Status from, Status to) {
        if (to.ordinal() != from.ordinal() + 1) {
            System.out.println("transition refusee: " + from + " -> " + to);
        } else {
            System.out.println("Facture " + id + ": " + from + " -> " + to);
        }
    }

    public static void main(String[] args) {
        Status status = Status.DRAFT;
        transition(1, status, Status.SENT);
        status = status.next();
        transition(1, status, Status.PAID);
        status = status.next();
        transition(1, status, Status.SENT);
        int net = 1999 + 2501;
        int total = net + net * 20 / 100;
        System.out.println("Total TTC: " + total / 100 + "." + String.format("%02d", total % 100));
    }
}
