import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    static final Map<String, String> DEFAULTS = Map.of("log.level", "INFO");
    static final Map<String, Map<String, String>> PROFILES = Map.of(
        "dev", Map.of("log.level", "DEBUG"),
        "prod", Map.of("log.level", "WARN"));

    static String logLevel(String profile) {
        return PROFILES.getOrDefault(profile, Map.of()).getOrDefault("log.level", DEFAULTS.get("log.level"));
    }

    public static void main(String[] args) {
        for (String profile : List.of("dev", "prod", "test")) {
            System.out.println(profile + ": niveau de log=" + logLevel(profile));
        }

        var counters = new TreeMap<Integer, Integer>();
        for (int status : List.of(200, 500, 200, 404, 200)) {
            counters.merge(status, 1, Integer::sum);
        }
        for (var entry : counters.entrySet()) {
            System.out.println("requests_total{status=\"" + entry.getKey() + "\"} " + entry.getValue());
        }
    }
}
