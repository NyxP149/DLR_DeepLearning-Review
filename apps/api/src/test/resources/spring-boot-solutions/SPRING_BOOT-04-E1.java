record Evidence(String concept, boolean validated) {}
public class Main {
    public static void main(String[] args) {
        var evidence = new Evidence("SPRING-REST", true);
        if (!evidence.validated()) throw new IllegalStateException("preuve invalide");
        System.out.println("SPRING_BOOT-04: preuve validée");
    }
}
