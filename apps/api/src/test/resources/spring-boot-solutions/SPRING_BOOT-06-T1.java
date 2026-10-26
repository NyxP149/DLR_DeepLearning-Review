import java.util.*;
import java.util.stream.Collectors;

public class Main {
    record Employee(String name, String department) {
    }

    record Page(List<Employee> content, int number, int totalPages) {
    }

    static Page page(List<Employee> all, int number, int size) {
        List<Employee> sorted = new ArrayList<>(all);
        sorted.sort(Comparator.comparing(Employee::department).thenComparing(Employee::name));
        int totalPages = (sorted.size() + size - 1) / size;
        int from = Math.min((number - 1) * size, sorted.size());
        int to = Math.min(from + size, sorted.size());
        return new Page(sorted.subList(from, to), number, totalPages);
    }

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Zoe", "RH"), new Employee("Ada", "IT"), new Employee("Linus", "IT"),
                new Employee("Bob", "RH"), new Employee("Nyx", "IT"), new Employee("Cleo", "Ventes"),
                new Employee("Dan", "RH"));
        for (int number = 1; number <= 4; number++) {
            Page page = page(employees, number, 3);
            String names = page.content().isEmpty()
                    ? "(vide)"
                    : page.content().stream().map(Employee::name).collect(Collectors.joining(", "));
            System.out.println("Page " + page.number() + "/" + page.totalPages() + ": " + names);
        }
    }
}
