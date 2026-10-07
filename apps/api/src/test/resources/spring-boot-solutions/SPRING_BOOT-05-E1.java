import java.util.ArrayList;
import java.util.List;

record CreateUserRequest(String name, String email, int age) {}

public class Main {
    static List<String> validate(CreateUserRequest request) {
        var errors = new ArrayList<String>();
        if (request.name() == null || request.name().isBlank()) {
            errors.add("name: ne doit pas être vide");
        }
        if (request.email() == null || !request.email().contains("@")) {
            errors.add("email: format invalide");
        }
        if (request.age() < 18) {
            errors.add("age: doit être >= 18");
        }
        return errors;
    }

    static String errorBody(List<String> errors) {
        var quoted = new ArrayList<String>();
        for (var error : errors) {
            quoted.add("\"" + error + "\"");
        }
        return "{\"status\":400,\"errors\":[" + String.join(",", quoted) + "]}";
    }

    public static void main(String[] args) {
        var invalid = new CreateUserRequest(" ", "ada-at-dlr.dev", 15);
        var valid = new CreateUserRequest("Ada", "ada@dlr.dev", 30);
        for (var request : List.of(invalid, valid)) {
            var errors = validate(request);
            if (errors.isEmpty()) {
                System.out.println("201 " + request.name());
            } else {
                System.out.println(errorBody(errors));
            }
        }
    }
}
