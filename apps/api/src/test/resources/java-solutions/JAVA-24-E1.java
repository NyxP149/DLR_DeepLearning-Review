import java.util.*;
import java.util.function.Predicate;

public class Main {
    record Candidate(String name, int score, boolean reviewed) {}
    record Rule(String name, Predicate<Candidate> test) {}

    public static void main(String[] args) {
        List<Candidate> candidates = List.of(
                new Candidate("Nyx", 90, true),
                new Candidate("Ada", 84, true),
                new Candidate("Linus", 85, false),
                new Candidate("", 10, false));
        List<Rule> rules = List.of(
                new Rule("nom", c -> !c.name().isBlank()),
                new Rule("score>=85", c -> c.score() >= 85),
                new Rule("relu", Candidate::reviewed));
        int accepted = 0;
        for (Candidate candidate : candidates) {
            List<String> failed = rules.stream().filter(rule -> !rule.test().test(candidate)).map(Rule::name).toList();
            if (failed.isEmpty()) {
                accepted++;
                System.out.println(candidate.name() + " : ACCEPTE");
            } else {
                System.out.println(candidate.name() + " : REFUSE " + failed);
            }
        }
        System.out.println("Bilan : " + accepted + "/" + candidates.size() + " acceptes");
    }
}
