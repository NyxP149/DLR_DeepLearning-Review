import java.util.*;

public class Main {
    record Request(String route, int status, long latencyMs, String trace) {}

    static long percentile(List<Long> sorted, double p) {
        int rank = (int) Math.ceil(p * sorted.size());
        return sorted.get(rank - 1);
    }

    public static void main(String[] args) {
        List<Request> requests = List.of(
                new Request("/orders", 200, 20, "t1"),
                new Request("/orders", 200, 25, "t2"),
                new Request("/orders", 200, 30, "t3"),
                new Request("/orders", 200, 40, "t4"),
                new Request("/orders", 200, 45, "t5"),
                new Request("/orders", 200, 50, "t6"),
                new Request("/orders", 503, 60, "t7"),
                new Request("/orders", 500, 900, "abc123"));
        long errors = requests.stream().filter(r -> r.status() >= 500).count();
        double rate = 100.0 * errors / requests.size();
        System.out.println("requetes=" + requests.size() + " erreurs=" + errors
                + " taux_erreur=" + String.format(Locale.ROOT, "%.1f", rate) + "%");

        List<Long> sorted = requests.stream().map(Request::latencyMs).sorted().toList();
        double mean = sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.println("moyenne=" + String.format(Locale.ROOT, "%.2f", mean) + "ms p50=" + percentile(sorted, 0.5)
                + "ms p95=" + percentile(sorted, 0.95) + "ms");

        Request slowest = requests.stream().max(Comparator.comparingLong(Request::latencyMs)).orElseThrow();
        String level = slowest.status() >= 500 ? "ERROR" : "INFO";
        System.out.println("level=" + level + " route=" + slowest.route() + " status=" + slowest.status()
                + " latence=" + slowest.latencyMs() + "ms trace=" + slowest.trace());
        System.out.println("Sante : " + (rate > 20 ? "DEGRADED" : "UP"));
    }
}
