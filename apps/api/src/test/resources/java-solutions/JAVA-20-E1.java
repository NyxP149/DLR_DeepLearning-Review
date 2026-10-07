interface Validator { boolean valid(String value); }
class OrderService {
    private final Validator validator;
    OrderService(Validator validator) { this.validator = validator; }
    String create(String order) { return validator.valid(order) ? "Service: commande validée" : "refusée"; }
}
public class Main {
    public static void main(String[] args) {
        System.out.println(new OrderService(value -> !value.isBlank()).create("commande"));
    }
}
