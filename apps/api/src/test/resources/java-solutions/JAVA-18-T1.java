import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        List<String> documents = List.of("un deux trois", "quatre cinq", "six sept huit neuf");
        AtomicInteger executed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (String document : documents) {
            tasks.add(() -> {
                executed.incrementAndGet();
                return document.split(" ").length;
            });
        }
        List<Integer> counts = new ArrayList<>();
        for (Future<Integer> future : pool.invokeAll(tasks)) counts.add(future.get());
        pool.shutdown();
        System.out.println("Mots par document : " + counts);
        System.out.println("Total mots : " + counts.stream().mapToInt(Integer::intValue).sum());
        System.out.println("Taches executees : " + executed.get());
    }
}
