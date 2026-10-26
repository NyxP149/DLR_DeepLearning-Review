import java.util.List;

public class Main {
    static int parseAge(String text) {
        int age;
        try {
            age = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("pas un nombre");
        }
        if (age < 0 || age > 120) throw new IllegalArgumentException("age hors limites");
        return age;
    }

    public static void main(String[] args) {
        List<String> saisies = List.of("25", "abc", "-3", "130", "42");
        int valid = 0;
        int refused = 0;
        for (String saisie : saisies) {
            try {
                System.out.println(saisie + " : " + parseAge(saisie));
                valid++;
            } catch (IllegalArgumentException exception) {
                System.out.println(saisie + " : refuse (" + exception.getMessage() + ")");
                refused++;
            }
        }
        System.out.println("Valides : " + valid + ", refuses : " + refused);
    }
}
