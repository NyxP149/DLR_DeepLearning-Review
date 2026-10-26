import java.util.*;
import java.util.function.Supplier;

public class Main {
    static class Container {
        private final Map<String, Supplier<Object>> factories = new HashMap<>();
        private final Set<String> singletons = new HashSet<>();
        private final Map<String, Object> instances = new HashMap<>();
        private final List<String> creationOrder = new ArrayList<>();

        void register(String name, boolean singleton, Supplier<Object> factory) {
            factories.put(name, factory);
            if (singleton) singletons.add(name);
        }

        Object get(String name) {
            if (!singletons.contains(name)) return factories.get(name).get();
            Object existing = instances.get(name);
            if (existing != null) return existing;
            Object created = factories.get(name).get();
            instances.put(name, created);
            creationOrder.add(name);
            System.out.println("Creation: " + name);
            return created;
        }

        List<String> shutdownOrder() {
            List<String> order = new ArrayList<>(creationOrder);
            Collections.reverse(order);
            return order;
        }
    }

    public static void main(String[] args) {
        Container container = new Container();
        container.register("Clock", true, Object::new);
        container.register("Mailer", true, () -> {
            container.get("Clock");
            return new Object();
        });
        container.register("RequestContext", false, Object::new);
        Object first = container.get("Mailer");
        Object second = container.get("Mailer");
        System.out.println("Meme instance singleton: " + (first == second));
        System.out.println("Meme instance prototype: " + (container.get("RequestContext") == container.get("RequestContext")));
        System.out.println("Ordre d'arret: " + String.join(", ", container.shutdownOrder()));
    }
}
