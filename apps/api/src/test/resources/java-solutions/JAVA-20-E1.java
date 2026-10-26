import java.util.*;

public class Main {
    interface OrderRepository {
        String save(String customer);
        int count();
    }

    static class InMemoryRepository implements OrderRepository {
        private int counter = 0;

        public String save(String customer) {
            counter++;
            return "commande-" + counter;
        }

        public int count() { return counter; }
    }

    static class OrderService {
        private final OrderRepository repository;

        OrderService(OrderRepository repository) { this.repository = repository; }

        String create(String customer) {
            if (customer.isBlank()) throw new IllegalArgumentException("client vide");
            return repository.save(customer);
        }
    }

    static class OrderController {
        private final OrderService service;

        OrderController(OrderService service) { this.service = service; }

        String post(String customer) {
            try {
                return "201 " + service.create(customer);
            } catch (IllegalArgumentException e) {
                return "400 " + e.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        InMemoryRepository repository = new InMemoryRepository();
        OrderController controller = new OrderController(new OrderService(repository));
        System.out.println(controller.post("Nyx"));
        System.out.println(controller.post(""));
        System.out.println(controller.post("Ada"));
        System.out.println("Depot : " + repository.count() + " commandes");

        int[] calls = {0};
        OrderRepository fake = new OrderRepository() {
            public String save(String customer) { calls[0]++; return "faux-1"; }
            public int count() { return calls[0]; }
        };
        new OrderService(fake).create("Nyx");
        System.out.println("Faux depot : " + calls[0] + " sauvegarde");
    }
}
