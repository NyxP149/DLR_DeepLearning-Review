import java.util.*;

public class Main {
    record Rule(String property, String dangerous, String advice) {
    }

    public static void main(String[] args) {
        Map<String, String> config = Map.of(
                "server.ssl.enabled", "false",
                "admin.password", "admin",
                "logging.level.root", "TRACE",
                "server.port", "8080");
        List<Rule> rules = List.of(
                new Rule("server.ssl.enabled", "false", "active TLS"),
                new Rule("admin.password", "admin", "definis un mot de passe fort via variable d'environnement"),
                new Rule("logging.level.root", "TRACE", "utilise INFO ou WARN en production"));
        List<String> violations = new ArrayList<>();
        for (Rule rule : rules) {
            if (rule.dangerous().equals(config.get(rule.property()))) {
                violations.add("- " + rule.property() + "=" + rule.dangerous() + " : " + rule.advice());
            }
        }
        Collections.sort(violations);
        System.out.println("Violations: " + violations.size());
        violations.forEach(System.out::println);
        System.out.println("Verdict: " + (violations.isEmpty() ? "ACCEPTEE" : "REFUSEE"));
    }
}
