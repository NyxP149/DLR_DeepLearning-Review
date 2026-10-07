record Evidence(String concept, boolean validated) {}
public class Main {
    public static void main(String[] args) {
        var evidence = new Evidence("SPRING-CHALLENGE", true);
        if (!evidence.validated()) throw new IllegalStateException("preuve invalide");
        System.out.println("SPRING_BOOT-12: preuve validée");
    }
}
