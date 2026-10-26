import java.util.*;
import java.util.stream.*;

public class Main {
    record Request(String route, int status, int ms) {
    }

    public static void main(String[] args) {
        List<Request> requests = List.of(
                new Request("/home", 200, 40), new Request("/orders", 500, 300),
                new Request("/home", 200, 60), new Request("/orders", 200, 100),
                new Request("/login", 401, 20), new Request("/orders", 503, 500));
        long errors = requests.stream().filter(r -> r.status() >= 500).count();
        Map<String, Double> average = requests.stream()
                .collect(Collectors.groupingBy(Request::route, TreeMap::new, Collectors.averagingInt(Request::ms)));
        Request slowest = requests.stream().max(Comparator.comparingInt(Request::ms)).orElseThrow();
        System.out.println("Erreurs serveur : " + errors);
        System.out.println("Moyenne par route : " + average);
        System.out.println("Plus lente : " + slowest.route() + " (" + slowest.ms() + ")");
    }
}
