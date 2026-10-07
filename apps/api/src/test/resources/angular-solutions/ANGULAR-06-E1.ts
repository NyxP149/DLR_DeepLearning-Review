export {};

interface Deferred {
  promise: Promise<string[]>;
  resolve: (value: string[]) => void;
}

function deferred(): Deferred {
  let resolve!: (value: string[]) => void;
  const promise = new Promise<string[]>((done) => {
    resolve = done;
  });
  return { promise, resolve };
}

class SearchBox {
  private latest = 0;
  state: 'idle' | 'loading' | 'done' = 'idle';

  async search(query: string, fetcher: (query: string) => Promise<string[]>): Promise<void> {
    const id = ++this.latest;
    this.state = 'loading';
    const results = await fetcher(query);
    if (id !== this.latest) {
      console.log(`ignoré: ${query}`);
      return;
    }
    this.state = 'done';
    console.log(`affiché: ${query} -> ${results.join(', ')}`);
  }
}

async function main(): Promise<void> {
  const box = new SearchBox();
  const slow = deferred();
  const fast = deferred();
  const first = box.search('a', () => slow.promise);
  const second = box.search('ab', () => fast.promise);
  console.log(`État: ${box.state}`);
  fast.resolve(['ab-1', 'ab-2']);
  await second;
  slow.resolve(['a-1']);
  await first;
  console.log(`État final: ${box.state}`);
}

main();
