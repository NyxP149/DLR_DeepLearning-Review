export {};

type Source = { version: number };

function signal<T>(initial: T) {
  let value = initial;
  const state: Source = { version: 0 };
  const read = (() => value) as (() => T) & { set(next: T): void; source: Source };
  read.set = (next: T) => {
    if (next !== value) {
      value = next;
      state.version += 1;
    }
  };
  read.source = state;
  return read;
}

function computed<T>(compute: () => T, sources: Source[]) {
  let cached: T;
  let seen: number[] | null = null;
  const stats = { recomputations: 0 };
  const read = (() => {
    const versions = sources.map((source) => source.version);
    if (seen === null || versions.some((version, index) => version !== seen![index])) {
      cached = compute();
      seen = versions;
      stats.recomputations += 1;
    }
    return cached;
  }) as (() => T) & { stats: typeof stats };
  read.stats = stats;
  return read;
}

const price = signal(100);
const taxRate = signal(20);
const ttc = computed(() => (price() * (100 + taxRate())) / 100, [price.source, taxRate.source]);

console.log(`Prix TTC: ${ttc()} (calculs: ${ttc.stats.recomputations})`);
console.log(`Prix TTC: ${ttc()} (calculs: ${ttc.stats.recomputations})`);
price.set(150);
console.log(`Prix TTC: ${ttc()} (calculs: ${ttc.stats.recomputations})`);
price.set(150);
console.log(`Prix TTC: ${ttc()} (calculs: ${ttc.stats.recomputations})`);
