import java.util.*;

public class Main {
    record Contact(String email, String name, String city) {
        @Override public boolean equals(Object other) {
            return other instanceof Contact contact && contact.email.equalsIgnoreCase(email);
        }

        @Override public int hashCode() {
            return email.toLowerCase().hashCode();
        }
    }

    public static void main(String[] args) {
        List<Contact> imported = List.of(
                new Contact("Ana@Mail.io", "Ana", "Lyon"),
                new Contact("bob@mail.io", "Bob", "Paris"),
                new Contact("ana@mail.io", "Ana B.", "Lyon"),
                new Contact("cleo@mail.io", "Cleo", "Lyon"),
                new Contact("BOB@MAIL.IO", "Bob 2", "Paris"),
                new Contact("dan@mail.io", "Dan", "Brest"));
        Set<Contact> unique = new LinkedHashSet<>(imported);
        List<Contact> sorted = new ArrayList<>(unique);
        sorted.sort(Comparator.comparing(Contact::city).thenComparing(Contact::name));
        for (Contact contact : sorted) {
            System.out.println(contact.city() + " : " + contact.name() + " <" + contact.email() + ">");
        }
        System.out.println("Fusionnes : " + (imported.size() - unique.size()));
    }
}
