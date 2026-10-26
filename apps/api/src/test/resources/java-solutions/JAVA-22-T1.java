import java.util.Arrays;
import java.util.Locale;

public class Main {
    static int percentile(int[] sorted, double p) {
        int rank = (int) Math.ceil(p * sorted.length);
        return sorted[rank - 1];
    }

    public static void main(String[] args) {
        int[] durations = {80, 120, 95, 150, 480, 60, 110, 130, 700, 90};
        int[] statuses = {200, 200, 200, 200, 200, 200, 503, 200, 200, 200};
        int errors = (int) Arrays.stream(statuses).filter(s -> s >= 500).count();
        double availability = 100.0 * (statuses.length - errors) / statuses.length;
        String formatted = String.format(Locale.ROOT, "%.1f", availability);
        int[] sorted = durations.clone();
        Arrays.sort(sorted);
        System.out.println("requetes=" + statuses.length + " erreurs5xx=" + errors + " disponibilite=" + formatted + "%");
        System.out.println("p50=" + percentile(sorted, 0.5) + "ms p90=" + percentile(sorted, 0.9) + "ms");
        System.out.println("level=WARN slo=99.0% disponibilite=" + formatted + "% statut=" + (availability < 99.0 ? "BREACH" : "OK"));
    }
}
