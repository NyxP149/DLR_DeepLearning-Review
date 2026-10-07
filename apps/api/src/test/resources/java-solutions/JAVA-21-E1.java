record OrderResponse(int status, String id) {}
public class Main {
    public static void main(String[] args) {
        OrderResponse response = new OrderResponse(201, "commande-42");
        System.out.println("HTTP " + response.status() + " | " + response.id());
    }
}
