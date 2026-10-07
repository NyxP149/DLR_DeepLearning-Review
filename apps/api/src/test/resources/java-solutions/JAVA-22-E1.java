record Health(String status, long latencyMs) {}
public class Main {
    public static void main(String[] args) {
        Health health = new Health("UP", 12);
        System.out.println("latence=" + health.latencyMs() + "ms status=" + health.status());
    }
}
