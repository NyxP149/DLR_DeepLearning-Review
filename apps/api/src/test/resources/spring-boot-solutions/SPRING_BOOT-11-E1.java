import java.util.List;
import java.util.Locale;

enum Status {
    CREATED, PAID, SHIPPED;

    boolean canMoveTo(Status next) {
        return next.ordinal() == ordinal() + 1;
    }
}

class OrderAggregate {
    private Status status = Status.CREATED;
    private final List<Integer> lineCents;

    OrderAggregate(List<Integer> lineCents) {
        this.lineCents = lineCents;
    }

    void moveTo(Status next) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException("transition refusée: " + status + " -> " + next);
        }
        System.out.println("Commande 1: " + status + " -> " + next);
        status = next;
    }

    String total() {
        int cents = 0;
        for (int line : lineCents) {
            cents += line;
        }
        return String.format(Locale.ROOT, "%d.%02d", cents / 100, cents % 100);
    }
}

public class Main {
    public static void main(String[] args) {
        var order = new OrderAggregate(List.of(2500, 2500, 3550));
        order.moveTo(Status.PAID);
        order.moveTo(Status.SHIPPED);
        try {
            order.moveTo(Status.PAID);
        } catch (IllegalStateException error) {
            System.out.println(error.getMessage());
        }
        System.out.println("Total: " + order.total());
    }
}
