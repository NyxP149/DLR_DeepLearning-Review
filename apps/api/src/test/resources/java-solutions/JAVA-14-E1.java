record Profil(String nom, int niveau) {
    Profil { if (niveau < 1) throw new IllegalArgumentException("niveau"); }
}
public class Main {
    public static void main(String[] args) {
        System.out.println(new Profil("Nyx", 3));
    }
}
