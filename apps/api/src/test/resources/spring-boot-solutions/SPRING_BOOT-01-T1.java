import java.util.*;

public class Main {
    record MailProperties(String host, int port, boolean tls) {
        static MailProperties bind(Map<String, String> properties) {
            String host = properties.get("host");
            if (host == null || host.isBlank()) throw new IllegalStateException("propriete manquante host");
            int port;
            try {
                port = Integer.parseInt(properties.getOrDefault("port", ""));
            } catch (NumberFormatException exception) {
                throw new IllegalStateException("port invalide " + properties.get("port"));
            }
            if (port < 1 || port > 65535) throw new IllegalStateException("port invalide " + port);
            return new MailProperties(host, port, Boolean.parseBoolean(properties.get("tls")));
        }
    }

    public static void main(String[] args) {
        List<Map<String, String>> configurations = List.of(
                Map.of("host", "smtp.mail.io", "port", "587", "tls", "true"),
                Map.of("host", "smtp.mail.io", "port", "70000", "tls", "false"),
                Map.of("port", "25", "tls", "false"));
        for (Map<String, String> configuration : configurations) {
            try {
                MailProperties properties = MailProperties.bind(configuration);
                System.out.println("Config chargee: " + properties.host() + ":" + properties.port() + " tls=" + properties.tls());
            } catch (IllegalStateException exception) {
                System.out.println("Demarrage refuse: " + exception.getMessage());
            }
        }
    }
}
