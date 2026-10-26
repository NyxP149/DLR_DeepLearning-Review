import java.util.*;
import java.util.function.Predicate;

public class Main {
    public static void main(String[] args) {
        List<String> codes = List.of("DLR-2026", "DLR-ABCD", "PROMO-26", "DLR-20");
        Map<String, Predicate<String>> rules = new LinkedHashMap<>();
        rules.put("prefixe", code -> code.startsWith("DLR-"));
        rules.put("longueur", code -> code.length() == 8);
        rules.put("chiffre", code -> !code.isEmpty() && Character.isDigit(code.charAt(code.length() - 1)));
        int valid = 0;
        for (String code : codes) {
            List<String> failed = new ArrayList<>();
            rules.forEach((name, rule) -> {
                if (!rule.test(code)) failed.add(name);
            });
            if (failed.isEmpty()) {
                System.out.println(code + " : VALIDE");
                valid++;
            } else {
                System.out.println(code + " : INVALIDE " + failed);
            }
        }
        System.out.println("Bilan : " + valid + "/" + codes.size() + " valides");
    }
}
