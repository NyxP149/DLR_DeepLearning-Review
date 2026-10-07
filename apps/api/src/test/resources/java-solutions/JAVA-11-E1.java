record Box<T>(T value) {}
public class Main {
    public static void main(String[] args) {
        Box<String> box = new Box<>("Java");
        System.out.println("Premier: " + box.value());
    }
}
