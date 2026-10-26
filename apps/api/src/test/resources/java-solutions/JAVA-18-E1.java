import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static int synced = 0;
    static final Object LOCK = new Object();

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Integer>> squares = new ArrayList<>();
            for (int n = 1; n <= 10; n++) {
                int value = n;
                squares.add(() -> value * value);
            }
            int sum = 0;
            for (Future<Integer> future : pool.invokeAll(squares)) sum += future.get();
            System.out.println("Somme des carres : " + sum);

            AtomicInteger atomic = new AtomicInteger();
            List<Callable<Void>> atomicTasks = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                atomicTasks.add(() -> {
                    for (int i = 0; i < 1000; i++) atomic.incrementAndGet();
                    return null;
                });
            }
            pool.invokeAll(atomicTasks);
            System.out.println("Compteur atomique : " + atomic.get());

            List<Callable<Void>> syncTasks = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                syncTasks.add(() -> {
                    for (int i = 0; i < 1000; i++) {
                        synchronized (LOCK) { synced++; }
                    }
                    return null;
                });
            }
            pool.invokeAll(syncTasks);
            System.out.println("Compteur synchronise : " + synced);

            Map<String, Integer> words = new ConcurrentHashMap<>();
            List<Callable<Void>> wordTasks = new ArrayList<>();
            for (String text : List.of("a b a", "b a", "c")) {
                wordTasks.add(() -> {
                    for (String word : text.split(" ")) words.merge(word, 1, Integer::sum);
                    return null;
                });
            }
            pool.invokeAll(wordTasks);
            System.out.println("Mots : " + new TreeMap<>(words));
        } finally {
            pool.shutdown();
        }
    }
}
