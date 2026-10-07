import java.util.List;
import java.util.Map;

record DbProperties(String url, int timeout, int maxPoolSize) {
    static DbProperties bind(Map<String, String> props) {
        String url = required(props, "url");
        int timeout = Integer.parseInt(required(props, "timeout"));
        int maxPoolSize = Integer.parseInt(props.getOrDefault("maxPoolSize", "10"));
        if (timeout < 1) {
            throw new IllegalStateException("timeout doit être >= 1");
        }
        return new DbProperties(url, timeout, maxPoolSize);
    }

    private static String required(Map<String, String> props, String key) {
        String value = props.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("propriété manquante " + key);
        }
        return value;
    }
}

public class Main {
    public static void main(String[] args) {
        var prod = Map.of("url", "jdbc:postgresql://prod-db:5432/orders", "timeout", "5", "maxPoolSize", "20");
        var typo = Map.of("url", "jdbc:postgresql://prod-db:5432/orders", "dbTimeout", "5");
        for (var props : List.of(prod, typo)) {
            try {
                var config = DbProperties.bind(props);
                System.out.println("Config chargée: " + config.url() + " timeout=" + config.timeout() + " pool=" + config.maxPoolSize());
            } catch (IllegalStateException error) {
                System.out.println("Démarrage refusé: " + error.getMessage());
            }
        }
    }
}
