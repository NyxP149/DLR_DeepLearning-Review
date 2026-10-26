public class Main {
    static int perimetre(int cote) {
        return 4 * cote;
    }

    static int perimetre(int largeur, int hauteur) {
        return 2 * (largeur + hauteur);
    }

    static double perimetre(double rayon) {
        return Math.round(2 * 3.14 * rayon * 100) / 100.0;
    }

    public static void main(String[] args) {
        System.out.println("carre 5 : " + perimetre(5));
        System.out.println("rectangle 3x4 : " + perimetre(3, 4));
        System.out.println("cercle 1.5 : " + perimetre(1.5));
    }
}
