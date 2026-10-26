public class Main {
    public static void main(String[] args) {
        String[] mots = {"radar", "java", "kayak", "jvm"};
        String longest = mots[0];
        for (String mot : mots) {
            String reversed = new StringBuilder(mot).reverse().toString();
            System.out.println(mot + " : " + mot.equals(reversed));
            if (mot.length() > longest.length()) longest = mot;
        }
        System.out.println("Plus long : " + longest);
    }
}
