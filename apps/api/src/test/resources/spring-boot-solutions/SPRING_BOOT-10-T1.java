import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Map<String, String>> overrides = Map.of(
                "dev", Map.of("log", "DEBUG"),
                "test", Map.of(),
                "prod", Map.of("pool", "20", "log", "WARN"));
        for (String profile : List.of("dev", "test", "prod")) {
            Map<String, String> config = new HashMap<>(Map.of("pool", "2", "log", "INFO"));
            config.putAll(overrides.get(profile));
            System.out.println(profile + ": pool=" + config.get("pool") + " log=" + config.get("log"));
        }
        int[] statuses = {200, 200, 500, 200};
        Map<Integer, Integer> counter = new TreeMap<>();
        for (int status : statuses) counter.merge(status, 1, Integer::sum);
        counter.forEach((status, count) ->
                System.out.println("http_requests_total{status=\"" + status + "\"} " + count));
    }
}
