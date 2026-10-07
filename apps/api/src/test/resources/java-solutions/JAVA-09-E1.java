public class Main {
    static int validate(int amount) {
        if (amount < 0) throw new IllegalArgumentException("montant négatif");
        return amount;
    }
    public static void main(String[] args) {
        try { validate(-1); }
        catch (IllegalArgumentException error) { System.out.println("Erreur: " + error.getMessage()); }
    }
}
