export {};

class WritableSignal<T> {
  version = 0;
  private value: T;

  constructor(initial: T) {
    this.value = initial;
  }

  get(): T {
    return this.value;
  }

  set(next: T): void {
    if (!Object.is(next, this.value)) {
      this.value = next;
      this.version++;
    }
  }
}

interface Computed<T> {
  get(): T;
  runs(): number;
}

function computed<T>(compute: () => T, sources: WritableSignal<unknown>[]): Computed<T> {
  let cached: T | undefined;
  let signature = '';
  let runs = 0;
  let computedOnce = false;
  return {
    get(): T {
      const current = sources.map((source) => source.version).join(',');
      if (!computedOnce || current !== signature) {
        cached = compute();
        signature = current;
        computedOnce = true;
        runs++;
      }
      return cached as T;
    },
    runs: () => runs,
  };
}

const price = new WritableSignal(10);
const quantity = new WritableSignal(3);
const total = computed(() => price.get() * quantity.get(), [price, quantity]);

console.log(`Total: ${total.get()} (calculs: ${total.runs()})`);
console.log(`Total: ${total.get()} (calculs: ${total.runs()})`);
quantity.set(5);
console.log(`Total: ${total.get()} (calculs: ${total.runs()})`);
quantity.set(5);
console.log(`Total: ${total.get()} (calculs: ${total.runs()})`);
