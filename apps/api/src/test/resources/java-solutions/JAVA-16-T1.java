import java.util.List;

public class Main {
    interface GradeScale {
        String mention(double average);
    }

    record Threshold(double veryGood, double good, double fair) implements GradeScale {
        public String mention(double average) {
            if (average >= veryGood) return "Tres bien";
            if (average >= good) return "Bien";
            if (average >= fair) return "Assez bien";
            return "Insuffisant";
        }
    }

    record Report(List<Double> notes, GradeScale scale) {
        double average() {
            return notes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        }

        String mention() {
            return scale.mention(average());
        }
    }

    static class ReportPrinter {
        void print(Report standard, Report strict) {
            System.out.println("Moyenne : " + standard.average());
            System.out.println("Mention (standard) : " + standard.mention());
            System.out.println("Mention (stricte) : " + strict.mention());
            System.out.println("Rapport: " + standard.notes().size() + " notes, moyenne " + standard.average());
        }
    }

    public static void main(String[] args) {
        List<Double> notes = List.of(12.0, 14.0, 16.0);
        Report standard = new Report(notes, new Threshold(16, 14, 12));
        Report strict = new Report(notes, new Threshold(18, 16, 14));
        new ReportPrinter().print(standard, strict);
    }
}
