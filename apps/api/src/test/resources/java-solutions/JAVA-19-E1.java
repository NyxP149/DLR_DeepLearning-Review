import java.util.*;
public class Main {
    public static void main(String[] args) {
        List<String> unitOfWork = new ArrayList<>();
        unitOfWork.add("insert-order");
        unitOfWork.add("update-stock");
        System.out.println("Transaction: COMMIT " + unitOfWork.size());
    }
}
