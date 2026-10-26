public class Main {
    public static void main(String[] args) {
        StringBuilder kept = new StringBuilder();
        int sum = 0;
        for (int n = 1; n <= 30; n++) {
            if (n % 3 == 0 && n % 2 != 0) {
                if (kept.length() > 0) kept.append(' ');
                kept.append(n);
                sum += n;
            }
        }
        System.out.println(kept);
        System.out.println("somme=" + sum);
        int value = 5;
        int doublings = 0;
        while (value <= 1000) {
            value *= 2;
            doublings++;
        }
        System.out.println("doublements=" + doublings + " valeur=" + value);
    }
}
