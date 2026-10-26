public class Main {
    interface Shipping { int cost(int total); }

    static Shipping shippingOf(String code) {
        return switch (code) {
            case "STANDARD" -> total -> 5;
            case "EXPRESS" -> total -> 15;
            case "FREE" -> total -> total >= 100 ? 0 : 5;
            default -> throw new IllegalArgumentException("mode inconnu : " + code);
        };
    }

    record Order(String customer, int total, String shipping, int shippingCost) {}

    static class OrderBuilder {
        private String customer;
        private int total;
        private String shipping = "STANDARD";

        OrderBuilder customer(String customer) { this.customer = customer; return this; }
        OrderBuilder total(int total) { this.total = total; return this; }
        OrderBuilder shipping(String shipping) { this.shipping = shipping; return this; }

        Order build() {
            if (customer == null || customer.isBlank()) throw new IllegalStateException("client requis");
            return new Order(customer, total, shipping, shippingOf(shipping).cost(total));
        }
    }

    public static void main(String[] args) {
        System.out.println("STANDARD(150) : " + shippingOf("STANDARD").cost(150));
        System.out.println("EXPRESS(150) : " + shippingOf("EXPRESS").cost(150));
        System.out.println("FREE(150) : " + shippingOf("FREE").cost(150));
        System.out.println("FREE(60) : " + shippingOf("FREE").cost(60));
        Order order = new OrderBuilder().customer("Nyx").total(150).shipping("EXPRESS").build();
        System.out.println("Commande : " + order);
        try {
            shippingOf("DRONE");
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        try {
            new OrderBuilder().total(10).build();
        } catch (IllegalStateException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
