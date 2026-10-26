import java.util.*;

public class Main {
    record Ticket(int id, String owner) {
    }

    interface TicketRepository {
        Ticket save(String owner);
        long countByOwner(String owner);
        int count();
    }

    static class InMemoryTicketRepository implements TicketRepository {
        private final List<Ticket> stored = new ArrayList<>();

        public Ticket save(String owner) {
            Ticket ticket = new Ticket(stored.size() + 1, owner);
            stored.add(ticket);
            return ticket;
        }

        public long countByOwner(String owner) {
            return stored.stream().filter(t -> t.owner().equals(owner)).count();
        }

        public int count() {
            return stored.size();
        }
    }

    static class TicketService {
        private final TicketRepository repository;

        TicketService(TicketRepository repository) {
            this.repository = repository;
        }

        Ticket open(String owner) {
            if (repository.countByOwner(owner) >= 3) {
                throw new IllegalStateException(owner + " a deja 3 tickets ouverts");
            }
            return repository.save(owner);
        }
    }

    public static void main(String[] args) {
        TicketRepository repository = new InMemoryTicketRepository();
        TicketService service = new TicketService(repository);
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                Ticket ticket = service.open("ana");
                System.out.println("Ticket " + ticket.id() + " cree pour " + ticket.owner());
            } catch (IllegalStateException exception) {
                System.out.println("Refuse: " + exception.getMessage());
            }
        }
        System.out.println("Persistes: " + repository.count());
    }
}
