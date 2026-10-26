import java.util.ArrayList;
import java.util.List;

public class Main {
    interface SubscriberRepository {
        void save(String email);
        int count();
    }

    static class MemoryRepository implements SubscriberRepository {
        private final List<String> emails = new ArrayList<>();
        public void save(String email) { emails.add(email); }
        public int count() { return emails.size(); }
    }

    static class FakeRepository implements SubscriberRepository {
        int saves = 0;
        public void save(String email) { saves++; }
        public int count() { return saves; }
    }

    static class SubscriberService {
        private final SubscriberRepository repository;

        SubscriberService(SubscriberRepository repository) {
            this.repository = repository;
        }

        String subscribe(String email) {
            if (!email.contains("@")) throw new IllegalArgumentException("email invalide");
            repository.save(email);
            return email;
        }
    }

    static class SubscriberController {
        private final SubscriberService service;

        SubscriberController(SubscriberService service) {
            this.service = service;
        }

        String post(String email) {
            try {
                return "201 " + service.subscribe(email);
            } catch (IllegalArgumentException exception) {
                return "400 " + exception.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        MemoryRepository repository = new MemoryRepository();
        SubscriberController controller = new SubscriberController(new SubscriberService(repository));
        System.out.println(controller.post("ana@mail.io"));
        System.out.println(controller.post("pas-un-email"));
        System.out.println(controller.post("bob@mail.io"));
        System.out.println("Depot : " + repository.count() + " abonnes");
        FakeRepository fake = new FakeRepository();
        new SubscriberController(new SubscriberService(fake)).post("zoe@mail.io");
        System.out.println("Faux depot : " + fake.saves + " sauvegarde");
    }
}
