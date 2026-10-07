interface Payment { String pay(int amount); }
class CardPayment implements Payment {
    public String pay(int amount) { return "Paiement carte: " + amount; }
}
public class Main {
    public static void main(String[] args) {
        Payment payment = new CardPayment();
        System.out.println(payment.pay(42));
    }
}
