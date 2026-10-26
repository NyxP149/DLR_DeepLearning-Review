import java.util.*;

public class Main {
    record CreateOrderRequest(String customer, int quantity) {}
    record OrderEntity(String id, String customer, int quantity, double internalCost) {}
    record OrderView(String id, String customer, int quantity) {}
    record Response(int status, String body) {}

    static final Map<String, OrderEntity> store = new LinkedHashMap<>();

    static Response post(CreateOrderRequest request) {
        List<String> errors = new ArrayList<>();
        if (request.customer() == null || request.customer().isBlank()) errors.add("client requis");
        if (request.quantity() <= 0) errors.add("quantite > 0");
        if (!errors.isEmpty()) return new Response(400, errors.toString());
        String id = "ord-" + (store.size() + 1);
        store.put(id, new OrderEntity(id, request.customer(), request.quantity(), 3.5));
        return new Response(201, "id=" + id);
    }

    static Response get(String id) {
        OrderEntity entity = store.get(id);
        if (entity == null) return new Response(404, "");
        OrderView view = new OrderView(entity.id(), entity.customer(), entity.quantity());
        return new Response(200, "{customer=" + view.customer() + ", quantity=" + view.quantity() + "}");
    }

    static String line(String label, Response response) {
        return label + " -> " + response.status() + (response.body().isEmpty() ? "" : " " + response.body());
    }

    public static void main(String[] args) {
        System.out.println(line("POST valide", post(new CreateOrderRequest("Nyx", 2))));
        System.out.println(line("POST invalide", post(new CreateOrderRequest("", 0))));
        System.out.println(line("GET ord-1", get("ord-1")));
        System.out.println(line("GET ord-9", get("ord-9")));
        boolean clean = Arrays.stream(OrderView.class.getRecordComponents()).noneMatch(c -> c.getName().equals("internalCost"));
        System.out.println("DTO sans cout interne : " + clean);
    }
}
