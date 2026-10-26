public class Main {
    public static void main(String[] args) {
        StringBuilder pairs = new StringBuilder();
        int sum = 0;
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                pairs.append(i).append(' ');
                sum += i;
            }
        }
        System.out.println(pairs.toString().trim());
        System.out.println("somme=" + sum);
        int power = 1;
        while (power <= 100) {
            power *= 2;
        }
        System.out.println("puissance=" + power);
    }
}
