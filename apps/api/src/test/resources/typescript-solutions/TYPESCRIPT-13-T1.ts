export {};

let calls = 0;
const flaky = async (): Promise<string> => {
  calls += 1;
  if (calls < 3) throw new Error('reseau');
  return 'ok';
};
const sleep = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

async function retry<T>(task: () => Promise<T>, times: number): Promise<T> {
  for (let attempt = 1; ; attempt++) {
    try {
      const value = await task();
      console.log(`tentative ${attempt}: succes`);
      return value;
    } catch (error) {
      console.log(`tentative ${attempt}: echec`);
      if (attempt >= times) throw error;
    }
  }
}

async function summarize(promises: Promise<unknown>[]): Promise<string> {
  const settled = await Promise.allSettled(promises);
  const fulfilled = settled.filter((item) => item.status === 'fulfilled').length;
  return `${fulfilled} reussies, ${settled.length - fulfilled} echouee`;
}

function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  const timeout = new Promise<never>((_, reject) => setTimeout(() => reject(new Error('delai depasse')), ms));
  return Promise.race([promise, timeout]);
}

async function main(): Promise<void> {
  console.log(`Resultat: ${await retry(flaky, 5)}`);
  console.log(`Regroupe: ${await summarize([Promise.resolve(1), Promise.reject(new Error('x')), Promise.resolve(3)])}`);
  try {
    await withTimeout(sleep(50), 5);
  } catch (error) {
    console.log(`Timeout: ${(error as Error).message}`);
  }
}

main();
