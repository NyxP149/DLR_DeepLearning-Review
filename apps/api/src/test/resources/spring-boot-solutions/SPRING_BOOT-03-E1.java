import java.util.ArrayList;
import java.util.List;

record Order(Integer id, List<String> items) {}

interface OrderRepository {
    Order save(Order order);

    int count();
}

class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> stored = new ArrayList<>();

    public Order save(Order order) {
        var saved = new Order(stored.size() + 1, order.items());
        stored.add(saved);
        return saved;
    }

    public int count() {
        return stored.size();
    }
}

class OrderService {
    private final OrderRepository repository;

    OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    Order create(List<String> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("la commande doit contenir au moins un article");
        }
        return repository.save(new Order(null, items));
    }
}

public class Main {
    public static void main(String[] args) {
        var repository = new InMemoryOrderRepository();
        var service = new OrderService(repository);

        var order = service.create(List.of("clavier", "souris", "écran"));
        System.out.println("Commande " + order.id() + " créée: " + order.items().size() + " articles");
        try {
            service.create(List.of());
        } catch (IllegalArgumentException error) {
            System.out.println("Refusée: " + error.getMessage());
        }
        System.out.println("Persistées: " + repository.count());
    }
}
