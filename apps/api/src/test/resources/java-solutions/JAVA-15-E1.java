public class Main {
    static int square(int value) { return value * value; }
    public static void main(String[] args) {
        int passed = 0;
        if (square(2) == 4) passed++;
        if (square(0) == 0) passed++;
        if (square(-3) == 9) passed++;
        System.out.println("Tests: " + passed + "/3");
    }
}
