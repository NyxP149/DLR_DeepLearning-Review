import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        String value = "DLR";
        int[] notes = {12, 8, 15, 10};
        String phrase = "Deep Learning Review";
        System.out.println(new StringBuilder(value).reverse().toString());
        int max = notes[0];
        int sum = 0;
        for (int note : notes) {
            if (note > max) max = note;
            sum += note;
        }
        System.out.println("max=" + max);
        double moyenne = (double) sum / notes.length;
        System.out.println("moyenne=" + String.format(Locale.ROOT, "%.2f", moyenne));
        StringBuilder initiales = new StringBuilder();
        for (String mot : phrase.split(" ")) {
            initiales.append(mot.charAt(0));
        }
        System.out.println(initiales);
    }
}
