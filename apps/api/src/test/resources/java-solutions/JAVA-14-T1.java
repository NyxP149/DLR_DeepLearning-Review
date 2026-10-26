import java.util.*;

public class Main {
    record Config(String nom, List<Integer> ports) {
        Config {
            if (nom == null || nom.isBlank()) throw new IllegalArgumentException("nom vide");
            ports = List.copyOf(ports);
        }

        Config withPort(int port) {
            List<Integer> copy = new ArrayList<>(ports);
            copy.add(port);
            return new Config(nom, copy);
        }

        Optional<Integer> findPort(int port) {
            return ports.stream().filter(p -> p == port).findFirst();
        }
    }

    public static void main(String[] args) {
        List<Integer> source = new ArrayList<>(List.of(80, 443));
        Config config = new Config("api", source);
        source.add(22);
        System.out.println(config);
        System.out.println("Original apres ajout externe : " + config.ports());
        System.out.println("Nouvelle : " + config.withPort(8080));
        System.out.println("Recherche 443 : " + config.findPort(443).map(p -> "trouve").orElse("absent"));
        System.out.println("Recherche 22 : " + config.findPort(22).map(p -> "trouve").orElse("absent"));
        try {
            new Config(" ", List.of());
        } catch (IllegalArgumentException exception) {
            System.out.println("Refuse : " + exception.getMessage());
        }
    }
}
