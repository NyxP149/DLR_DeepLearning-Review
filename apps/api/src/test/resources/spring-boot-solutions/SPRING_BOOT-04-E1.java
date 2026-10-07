public class Main {
    static int handle(String method, String path, String accept) {
        if (!accept.equals("application/json") && !accept.equals("*/*")) {
            return 406;
        }
        boolean collection = path.equals("/orders");
        boolean item = path.matches("/orders/[0-9]+");
        if (!collection && !item) {
            return 404;
        }
        if (collection && method.equals("POST")) {
            return 201;
        }
        if (item && method.equals("GET")) {
            return 200;
        }
        return 405;
    }

    public static void main(String[] args) {
        String[][] requests = {
            {"GET", "/orders/42", "application/json"},
            {"POST", "/orders", "application/json"},
            {"DELETE", "/orders/42", "application/json"},
            {"GET", "/customers", "application/json"},
            {"GET", "/orders/42", "application/xml"},
        };
        for (String[] request : requests) {
            System.out.println(request[0] + " " + request[1] + " (" + request[2] + ") -> " + handle(request[0], request[1], request[2]));
        }
    }
}
