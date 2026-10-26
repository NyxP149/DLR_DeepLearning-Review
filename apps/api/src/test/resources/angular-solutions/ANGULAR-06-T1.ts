export {};

type State =
  | { kind: 'loading' }
  | { kind: 'success'; results: string[] }
  | { kind: 'error'; message: string };

const requests: (() => Promise<string[]>)[] = [
  async () => ['a', 'b', 'c'],
  async () => { throw new Error('timeout'); },
  async () => ['d'],
];

async function safe(request: () => Promise<string[]>): Promise<State> {
  try {
    return { kind: 'success', results: await request() };
  } catch (error) {
    return { kind: 'error', message: (error as Error).message };
  }
}

async function main(): Promise<void> {
  let last: State = { kind: 'loading' };
  for (let index = 0; index < requests.length; index++) {
    last = await safe(requests[index]);
    if (last.kind === 'success') {
      console.log(`requete ${index + 1}: success (${last.results.length} resultats)`);
    } else if (last.kind === 'error') {
      console.log(`requete ${index + 1}: erreur (${last.message}) -> flux conserve`);
    }
  }
  console.log(`Etat final: ${last.kind}`);
}

main();
