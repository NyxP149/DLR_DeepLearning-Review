export {};

let naiveCalls = 0;
function naive(n: number, k: number): number {
  naiveCalls += 1;
  if (k === 0 || k === n) return 1;
  return naive(n - 1, k - 1) + naive(n - 1, k);
}

let cachedCalls = 0;
const cache = new Map<string, number>();
function memoized(n: number, k: number): number {
  cachedCalls += 1;
  if (k === 0 || k === n) return 1;
  const key = `${n}:${k}`;
  const known = cache.get(key);
  if (known !== undefined) return known;
  const value = memoized(n - 1, k - 1) + memoized(n - 1, k);
  cache.set(key, value);
  return value;
}

naive(10, 5);
memoized(10, 5);
console.log(`binomial(10,5) naif: ${naiveCalls} appels`);
console.log(`binomial(10,5) memoise: ${cachedCalls} appels`);
const budget = 100;
console.log(`Budget ${budget} appels: naif ${naiveCalls > budget ? 'depasse' : 'respecte'}, memoise ${cachedCalls > budget ? 'depasse' : 'respecte'}`);
