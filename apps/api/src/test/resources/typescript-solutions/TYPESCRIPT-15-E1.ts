export {};

async function* chunks(): AsyncGenerator<string> {
  for (const part of ['alp', 'ha\nbe', 'ta\ngam', 'ma']) {
    yield part;
  }
}

async function* lines(source: AsyncIterable<string>): AsyncGenerator<string> {
  let buffer = '';
  for await (const chunk of source) {
    buffer += chunk;
    const parts = buffer.split('\n');
    buffer = parts.pop() ?? '';
    for (const line of parts) {
      yield line;
    }
  }
  if (buffer !== '') {
    yield buffer;
  }
}

async function main(): Promise<void> {
  let count = 0;
  for await (const line of lines(chunks())) {
    count++;
    console.log(`Ligne: ${line}`);
  }
  console.log(`Lignes: ${count}`);
}

main();
