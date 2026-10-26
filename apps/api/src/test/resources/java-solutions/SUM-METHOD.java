public class Main {
    static int sum(int a, int b) {
        return a + b;
    }

    static int sum(int a, int b, int c) {
        return a + b + c;
    }

    static double sum(double a, double b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println(sum(5, 7));
        System.out.println(sum(1, 2, 3));
        System.out.println(sum(1.5, 2.0));
    }
}
