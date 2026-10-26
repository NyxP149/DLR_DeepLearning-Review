import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> connexions = List.of("zoe", "ana", "zoe", "bob", "ana", "zoe", "cleo", "bob");
        Map<String, Integer> parUtilisateur = new HashMap<>();
        for (String utilisateur : connexions) {
            parUtilisateur.merge(utilisateur, 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> classement = new ArrayList<>(parUtilisateur.entrySet());
        classement.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        for (Map.Entry<String, Integer> entree : classement) {
            System.out.println(entree.getKey() + " : " + entree.getValue());
        }
        System.out.println("Utilisateurs distincts : " + parUtilisateur.size());
    }
}
