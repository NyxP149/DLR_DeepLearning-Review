import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        int total = 100;
        int people = 8;
        int share = total / people;
        int rest = total % people;
        double exact = (double) total / people;
        System.out.println("part entiere=" + share);
        System.out.println("reste=" + rest);
        System.out.println("part exacte=" + String.format(Locale.ROOT, "%.2f", exact));
        System.out.println("total recompose=" + (share * people + rest));
    }
}
