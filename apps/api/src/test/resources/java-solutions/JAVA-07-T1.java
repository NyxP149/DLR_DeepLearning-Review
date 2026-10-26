import java.util.List;

public class Main {
    static abstract class Notification {
        protected final String to;
        protected final String message;

        Notification(String to, String message) {
            this.to = to;
            this.message = message;
        }

        abstract String send();
    }

    static class Email extends Notification {
        Email(String to, String message) { super(to, message); }
        String send() { return "EMAIL a " + to + " : " + message; }
    }

    static class Sms extends Notification {
        Sms(String to, String message) { super(to, message); }
        String send() { return "SMS a " + to + " : " + message; }
    }

    static class Push extends Notification {
        Push(String to, String message) { super(to, message); }
        String send() { return "PUSH a " + to + " : " + message; }
    }

    public static void main(String[] args) {
        List<Notification> all = List.of(
                new Email("nyx@mail.io", "Bienvenue"),
                new Sms("+33600000000", "Bienvenue"),
                new Push("nyx", "Bienvenue"));
        for (Notification notification : all) {
            System.out.println(notification.send());
        }
        System.out.println(all.size() + " notifications envoyees");
    }
}
