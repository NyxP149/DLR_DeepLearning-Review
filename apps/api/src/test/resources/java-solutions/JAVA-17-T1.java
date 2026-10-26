import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
    interface Formatter {
        String format(String text);
    }

    static Formatter formatterOf(String code) {
        return switch (code) {
            case "UPPER" -> String::toUpperCase;
            case "LOWER" -> String::toLowerCase;
            case "TITLE" -> text -> Arrays.stream(text.split(" "))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
            default -> throw new IllegalArgumentException("format inconnu " + code);
        };
    }

    public static void main(String[] args) {
        String text = "bonjour nyx";
        for (String code : new String[] {"UPPER", "LOWER", "TITLE"}) {
            System.out.println(code + " : " + formatterOf(code).format(text));
        }
        try {
            formatterOf("REVERSE");
        } catch (IllegalArgumentException exception) {
            System.out.println("Erreur : " + exception.getMessage());
        }
    }
}
