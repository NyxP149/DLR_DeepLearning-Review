import java.util.*;
public class Main {
    public static void main(String[] args) {
        Set<String> unique = new TreeSet<>(List.of("Linus", "Ada", "Grace", "Ada"));
        System.out.println(String.join(", ", unique));
    }
}
