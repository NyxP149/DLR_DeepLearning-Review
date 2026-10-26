import java.util.*;

public class Main {
    static final Map<String, String> TOKENS = Map.of("t-ana", "USER", "t-max", "MANAGER");

    static int handle(String path, String token) {
        if (path.equals("/health")) return 200;
        String role = token == null ? null : TOKENS.get(token);
        if (role == null) return 401;
        if (path.equals("/reports/export") && !role.equals("MANAGER")) return 403;
        return 200;
    }

    public static void main(String[] args) {
        String[][] requests = {
                {"/health", null}, {"/reports", null}, {"/reports", "t-ana"},
                {"/reports/export", "t-ana"}, {"/reports/export", "t-max"}};
        for (String[] request : requests) {
            String shown = request[1] == null ? "sans jeton" : request[1];
            System.out.println("GET " + request[0] + " [" + shown + "] -> " + handle(request[0], request[1]));
        }
    }
}
