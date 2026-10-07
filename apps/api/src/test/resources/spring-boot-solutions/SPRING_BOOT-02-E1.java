import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class Container {
    private final Map<Class<?>, Function<Container, ?>> factories = new HashMap<>();
    private final Map<Class<?>, Object> singletons = new LinkedHashMap<>();

    <T> void register(Class<T> type, Function<Container, T> factory) {
        factories.put(type, factory);
    }

    <T> T get(Class<T> type) {
        Object existing = singletons.get(type);
        if (existing != null) {
            return type.cast(existing);
        }
        T bean = type.cast(factories.get(type).apply(this));
        System.out.println("Création: " + type.getSimpleName());
        singletons.put(type, bean);
        return bean;
    }

    List<String> shutdownOrder() {
        var names = new ArrayList<String>();
        for (var type : singletons.keySet()) {
            names.add(type.getSimpleName());
        }
        Collections.reverse(names);
        return names;
    }
}

record OrderRepository() {}

record OrderService(OrderRepository repository) {}

public class Main {
    public static void main(String[] args) {
        var container = new Container();
        container.register(OrderRepository.class, c -> new OrderRepository());
        container.register(OrderService.class, c -> new OrderService(c.get(OrderRepository.class)));

        var first = container.get(OrderService.class);
        var second = container.get(OrderService.class);
        System.out.println("Même instance: " + (first == second));
        System.out.println("Ordre d'arrêt: " + String.join(", ", container.shutdownOrder()));
    }
}
