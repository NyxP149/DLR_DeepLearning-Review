import java.util.List;
record Candidate(String name, int score, boolean reviewed) {}
public class Main {
    public static void main(String[] args) {
        Candidate candidate = new Candidate("Nyx", 90, true);
        List<Boolean> rules = List.of(!candidate.name().isBlank(), candidate.score() >= 85, candidate.reviewed());
        long passed = rules.stream().filter(Boolean::booleanValue).count();
        if (passed != 3) throw new AssertionError("défi incomplet");
        System.out.println("Défi validé: " + passed + " règles");
    }
}
