import java.util.*;

public class Main {
    record Profil(String nom, int niveau, List<String> badges) {
        Profil {
            if (niveau < 1) throw new IllegalArgumentException("niveau invalide");
            badges = List.copyOf(badges);
        }

        Profil withNiveau(int nouveau) {
            return new Profil(nom, nouveau, badges);
        }
    }

    static Optional<Profil> chercher(List<Profil> profils, String nom) {
        return profils.stream().filter(p -> p.nom().equals(nom)).findFirst();
    }

    public static void main(String[] args) {
        List<String> badges = new ArrayList<>(List.of("java", "sql"));
        Profil nyx = new Profil("Nyx", 3, badges);
        System.out.println(nyx);
        System.out.println("Niveau suivant : " + nyx.withNiveau(4));
        System.out.println("Original : " + nyx.niveau());
        badges.add("docker");
        System.out.println("Badges proteges : " + nyx.badges());
        try {
            new Profil("Bug", 0, badges);
        } catch (IllegalArgumentException e) {
            System.out.println("Refuse : " + e.getMessage());
        }
        List<Profil> profils = List.of(nyx);
        System.out.println("Ada : " + chercher(profils, "Ada").map(Profil::nom).orElse("absent"));
        System.out.println("Nyx : " + chercher(profils, "Nyx").map(Profil::nom).orElse("absent"));
    }
}
