export {};

let naiveCalls = 0;
let memoCalls = 0;

function fibNaive(n: number): number {
  naiveCalls++;
  return n < 2 ? n : fibNaive(n - 1) + fibNaive(n - 2);
}

const cache = new Map<number, number>();

function fibMemo(n: number): number {
  memoCalls++;
  if (n < 2) {
    return n;
  }
  const hit = cache.get(n);
  if (hit !== undefined) {
    return hit;
  }
  const value = fibMemo(n - 1) + fibMemo(n - 2);
  cache.set(n, value);
  return value;
}

const BUDGET = 100;
fibNaive(20);
fibMemo(20);
console.log(`fib(20) naïf: ${naiveCalls} appels`);
console.log(`fib(20) mémoïsé: ${memoCalls} appels`);
console.log(
  `Budget ${BUDGET} appels: naïf ${naiveCalls > BUDGET ? 'dépasse' : 'respecte'}, mémoïsé ${memoCalls > BUDGET ? 'dépasse' : 'respecte'}`,
);
