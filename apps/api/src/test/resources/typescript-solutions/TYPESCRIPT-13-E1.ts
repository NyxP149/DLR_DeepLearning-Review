export {};

async function mapWithLimit<T, R>(items: T[], limit: number, worker: (item: T) => Promise<R>): Promise<R[]> {
  const results: R[] = new Array(items.length);
  let next = 0;

  async function runner(): Promise<void> {
    while (next < items.length) {
      const index = next++;
      results[index] = await worker(items[index]);
    }
  }

  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, runner));
  return results;
}

const sleep = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

let inFlight = 0;
let maxInFlight = 0;

async function double(value: number): Promise<number> {
  inFlight++;
  maxInFlight = Math.max(maxInFlight, inFlight);
  await sleep(10);
  inFlight--;
  return value * 2;
}

async function main(): Promise<void> {
  const results = await mapWithLimit([1, 2, 3, 4, 5], 2, double);
  console.log(`Résultats: ${JSON.stringify(results)}`);
  console.log(`Concurrence maximale: ${maxInFlight}`);
}

main();
