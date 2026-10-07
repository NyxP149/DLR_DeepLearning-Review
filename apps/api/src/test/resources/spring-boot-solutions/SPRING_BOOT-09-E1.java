import java.util.Map;

public class Main {
    static final Map<String, String> ROLES = Map.of("t-alice", "USER", "t-bob", "ADMIN");

    static int authorize(String path, String token) {
        if (path.equals("/public")) {
            return 200;
        }
        if (!path.equals("/admin")) {
            return 404;
        }
        if (token == null || !ROLES.containsKey(token)) {
            return 401;
        }
        return ROLES.get(token).equals("ADMIN") ? 200 : 403;
    }

    public static void main(String[] args) {
        String[][] calls = {
            {"/public", null},
            {"/admin", null},
            {"/admin", "t-alice"},
            {"/admin", "t-bob"},
        };
        for (String[] call : calls) {
            String shown = call[1] == null ? "sans jeton" : call[1];
            System.out.println("GET " + call[0] + " [" + shown + "] -> " + authorize(call[0], call[1]));
        }
    }
}
