export {};

class EventEmitter<T> {
  private listeners: ((value: T) => void)[] = [];

  subscribe(listener: (value: T) => void): void {
    this.listeners.push(listener);
  }

  emit(value: T): void {
    this.listeners.forEach((listener) => listener(value));
  }
}

class Counter {
  readonly changed = new EventEmitter<number>();
  private value = 0;

  constructor(private readonly max: number) {}

  increment(): void {
    const next = Math.min(this.value + 1, this.max);
    if (next === this.value) return;
    this.value = next;
    this.changed.emit(next);
  }

  get current(): number {
    return this.value;
  }

  get capped(): boolean {
    return this.value === this.max;
  }
}

const counter = new Counter(3);
counter.changed.subscribe((value) => console.log(`Parent: valeur recue ${value}`));
for (let press = 0; press < 4; press++) counter.increment();
console.log(`Composant: valeur ${counter.current}${counter.capped ? ' (plafonnee a 3)' : ''}`);
