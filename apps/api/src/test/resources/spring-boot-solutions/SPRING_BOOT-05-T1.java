import java.util.*;

public class Main {
    record CreateOrderRequest(String customer, int quantity, String coupon) {
    }

    static List<String> validate(CreateOrderRequest request) {
        List<String> errors = new ArrayList<>();
        if (request.customer() == null || request.customer().isBlank()) errors.add("customer: ne doit pas etre vide");
        if (request.quantity() < 1) errors.add("quantity: doit etre >= 1");
        if (request.coupon() != null && !request.coupon().matches("[A-Z]{4}")) errors.add("coupon: 4 lettres majuscules");
        return errors;
    }

    static String errorBody(List<String> errors) {
        StringBuilder body = new StringBuilder("{\"status\":400,\"errors\":[");
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) body.append(',');
            body.append('"').append(errors.get(i)).append('"');
        }
        return body.append("]}").toString();
    }

    public static void main(String[] args) {
        List<CreateOrderRequest> requests = List.of(
                new CreateOrderRequest("Ada", 2, "PROM"),
                new CreateOrderRequest("", 0, "promo"));
        for (CreateOrderRequest request : requests) {
            List<String> errors = validate(request);
            System.out.println(errors.isEmpty() ? "201 " + request.customer() : errorBody(errors));
        }
    }
}
