import java.util.List;
public class Main {
    public static void main(String[] args) {
        int sum = List.of(1, 2, 4, 6, 7).stream().filter(n -> n % 2 == 0).mapToInt(Integer::intValue).sum();
        System.out.println("Somme: " + sum);
    }
}
