import java.util.concurrent.*;
public class Main {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> future = executor.submit(() -> 40 + 2);
            System.out.println("Résultat: " + future.get());
        } finally { executor.shutdown(); }
    }
}
