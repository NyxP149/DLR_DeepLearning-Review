public class Main {
    static class Stock {
        private int quantity = 10;

        boolean add(int amount) {
            if (amount <= 0) return false;
            quantity += amount;
            return true;
        }

        boolean remove(int amount) {
            if (amount <= 0 || amount > quantity) return false;
            quantity -= amount;
            return true;
        }

        int quantity() {
            return quantity;
        }
    }

    public static void main(String[] args) {
        Stock stock = new Stock();
        System.out.println("Stock: " + stock.quantity());
        System.out.println("Ajout -5: " + (stock.add(-5) ? "accepte" : "refuse"));
        System.out.println("Retrait 20: " + (stock.remove(20) ? "accepte" : "refuse"));
        System.out.println("Retrait 4: " + (stock.remove(4) ? "accepte" : "refuse"));
        System.out.println("Stock final: " + stock.quantity());
    }
}
