import java.util.*;

public class Main {
    record Request(String method, String path, String accept) {
    }

    static int route(Request request) {
        boolean item = request.path().matches("/books/[^/]+");
        boolean collection = request.path().equals("/books");
        if (!item && !collection) return 404;
        String allowed = item ? "GET" : "POST";
        if (!request.method().equals(allowed)) return 405;
        if (!request.accept().equals("application/json") && !request.accept().equals("*/*")) return 406;
        return item ? 200 : 201;
    }

    public static void main(String[] args) {
        List<Request> requests = List.of(
                new Request("GET", "/books/7", "application/json"),
                new Request("POST", "/books", "application/json"),
                new Request("DELETE", "/books/7", "application/json"),
                new Request("GET", "/authors", "application/json"),
                new Request("GET", "/books/7", "text/xml"));
        for (Request request : requests) {
            System.out.println(request.method() + " " + request.path() + " (" + request.accept() + ") -> " + route(request));
        }
    }
}
