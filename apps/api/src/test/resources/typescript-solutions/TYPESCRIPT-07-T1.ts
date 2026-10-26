export {};

abstract class Employee {
  private base: number;

  constructor(protected readonly name: string, base: number) {
    this.base = base;
  }

  protected abstract bonusPercent(): number;

  raise(percent: number): void {
    if (percent < 0) throw new Error('augmentation negative');
    this.base += (this.base * percent) / 100;
  }

  total(): number {
    return this.base + (this.base * this.bonusPercent()) / 100;
  }

  describe(role: string): string {
    return `${this.name} (${role}): total ${this.total()}`;
  }
}

class Developer extends Employee {
  protected bonusPercent(): number {
    return 10;
  }
}

class Manager extends Employee {
  protected bonusPercent(): number {
    return 20;
  }
}

const ada = new Developer('Ada', 3000);
const bob = new Manager('Bob', 4000);
console.log(ada.describe('dev'));
console.log(bob.describe('manager'));
console.log(`Masse salariale: ${ada.total() + bob.total()}`);
try {
  ada.raise(-5);
} catch (error) {
  console.log(`Refus: ${(error as Error).message}`);
}
