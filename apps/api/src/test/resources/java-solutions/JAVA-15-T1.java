public class Main {
    static int passed = 0;
    static int total = 0;

    static void check(String name, boolean ok) {
        total++;
        if (ok) passed++;
        System.out.println((ok ? "PASS " : "FAIL ") + name);
    }

    static boolean isStrong(String password) {
        boolean digit = password.chars().anyMatch(Character::isDigit);
        boolean upper = password.chars().anyMatch(Character::isUpperCase);
        return password.length() >= 8 && digit && upper;
    }

    public static void main(String[] args) {
        check("trop court refuse", !isStrong("Ab1"));
        check("sans chiffre refuse", !isStrong("Abcdefghij"));
        check("sans majuscule refuse", !isStrong("abcdefg1h"));
        check("valide accepte", isStrong("Abcdefg123"));
        check("limite 8 caracteres accepte", isStrong("Abcdef12"));
        System.out.println("Resultat : " + passed + "/" + total);
    }
}
