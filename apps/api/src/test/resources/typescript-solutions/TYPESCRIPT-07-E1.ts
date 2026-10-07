export {};

abstract class Account {
  protected balance: number;
  private history: string[] = [];

  constructor(initial: number) {
    this.balance = initial;
  }

  abstract fee(): number;

  withdraw(amount: number): void {
    const cost = amount + this.fee();
    if (cost > this.balance) {
      throw new Error('solde insuffisant');
    }
    this.balance -= cost;
    this.history.push(`retrait ${amount}`);
  }

  summary(): string {
    return `solde ${this.balance} (frais ${this.fee()})`;
  }

  log(): string {
    return this.history.join(', ');
  }
}

class Checking extends Account {
  fee(): number {
    return 1;
  }
}

class Savings extends Account {
  fee(): number {
    return 0;
  }
}

const checking = new Checking(100);
const savings = new Savings(100);
checking.withdraw(10);
savings.withdraw(10);
console.log(`Courant: ${checking.summary()}`);
console.log(`Épargne: ${savings.summary()}`);
console.log(`Historique courant: ${checking.log()}`);
try {
  savings.withdraw(500);
} catch (error) {
  console.log(`Refus: ${(error as Error).message}`);
}
