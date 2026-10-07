import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

record Rule(String key, String forbidden, String advice) {}

public class Main {
    static final List<Rule> RULES = List.of(
        new Rule("actuator.exposure", "*", "expose uniquement health,info"),
        new Rule("cors.allowed-origins", "*", "liste les origines autorisées"),
        new Rule("debug", "true", "désactive le debug en production"),
        new Rule("db.password", "admin", "lis le mot de passe depuis un secret"));

    static List<String> audit(Map<String, String> config) {
        var violations = new ArrayList<String>();
        for (var rule : RULES) {
            if (rule.forbidden().equals(config.get(rule.key()))) {
                violations.add(rule.key() + "=" + config.get(rule.key()) + " : " + rule.advice());
            }
        }
        Collections.sort(violations);
        return violations;
    }

    public static void main(String[] args) {
        var config = new TreeMap<>(Map.of(
            "actuator.exposure", "*", "cors.allowed-origins", "*", "debug", "true",
            "db.password", "s3cr3t", "server.port", "8080"));
        var violations = audit(config);
        System.out.println("Violations: " + violations.size());
        for (var violation : violations) {
            System.out.println("- " + violation);
        }
        System.out.println(violations.isEmpty() ? "Verdict: ACCEPTÉ" : "Verdict: REFUSÉ");
    }
}
