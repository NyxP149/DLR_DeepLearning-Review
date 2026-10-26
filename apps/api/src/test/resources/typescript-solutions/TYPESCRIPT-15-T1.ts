export {};

async function* chunks(): AsyncIterable<string> {
  for (const piece of ['ad', 'a;lin', 'us;n', 'yx']) yield piece;
}

async function* records(source: AsyncIterable<string>): AsyncGenerator<string> {
  let buffer = '';
  for await (const chunk of source) {
    buffer += chunk;
    let index = buffer.indexOf(';');
    while (index >= 0) {
      yield buffer.slice(0, index);
      buffer = buffer.slice(index + 1);
      index = buffer.indexOf(';');
    }
  }
  if (buffer.length > 0) yield buffer;
}

async function main(): Promise<void> {
  let count = 0;
  for await (const record of records(chunks())) {
    console.log(`Enregistrement: ${record}`);
    count += 1;
  }
  console.log(`Enregistrements: ${count}`);
}

main();
