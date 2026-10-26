import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    record Response(int status, String body) {
        @Override public String toString() {
            return status + (body.isEmpty() ? "" : " " + body);
        }
    }

    static final Set<String> tasks = new HashSet<>();
    static int sequence = 0;

    static Response post(String title, int priority) {
        List<String> errors = new ArrayList<>();
        if (title == null || title.isBlank()) errors.add("titre requis");
        if (priority < 1 || priority > 3) errors.add("priorite entre 1 et 3");
        if (!errors.isEmpty()) return new Response(400, errors.toString());
        String id = "t-" + (++sequence);
        tasks.add(id);
        return new Response(201, "id=" + id);
    }

    static Response delete(String id) {
        return tasks.remove(id) ? new Response(204, "") : new Response(404, "");
    }

    public static void main(String[] args) {
        System.out.println("POST valide -> " + post("Ecrire le rapport", 2));
        System.out.println("POST invalide -> " + post("", 5));
        System.out.println("DELETE t-1 -> " + delete("t-1"));
        System.out.println("DELETE t-1 -> " + delete("t-1"));
    }
}
