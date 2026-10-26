class BankAccount {
    private int balance;

    BankAccount() {
        this.balance = 100;
    }

    boolean deposit(int amount) {
        if (amount <= 0) return false;
        balance += amount;
        return true;
    }

    boolean withdraw(int amount) {
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        return true;
    }

    int getBalance() {
        return balance;
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount account = new BankAccount();
        account.deposit(50);
        System.out.println("Solde: " + account.getBalance());
        if (!account.deposit(-20)) {
            System.out.println("Dépôt -20: refusé");
        }
        if (!account.withdraw(500)) {
            System.out.println("Retrait 500: refusé");
        }
        account.withdraw(30);
        System.out.println("Solde final: " + account.getBalance());
    }
}
