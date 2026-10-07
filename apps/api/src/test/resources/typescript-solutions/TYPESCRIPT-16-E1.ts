export {};

let passed = 0;
let failed = 0;

function assertEqual<T>(actual: T, expected: T): void {
  if (actual !== expected) {
    throw new Error(`attendu ${expected}, obtenu ${actual}`);
  }
}

function test(name: string, body: () => void): void {
  try {
    body();
    passed++;
    console.log(`✓ ${name}`);
  } catch (error) {
    failed++;
    console.log(`✗ ${name}: ${(error as Error).message}`);
  }
}

function spy<A extends unknown[]>(): { fn: (...args: A) => void; calls: A[] } {
  const calls: A[] = [];
  return {
    fn: (...args: A) => {
      calls.push(args);
    },
    calls,
  };
}

function notify(log: (message: string) => void): void {
  log('ok');
}

test('additionne deux nombres', () => assertEqual(2 + 2, 4));
test('le logger est appelé une fois avec "ok"', () => {
  const logger = spy<[string]>();
  notify(logger.fn);
  assertEqual(logger.calls.length, 1);
  assertEqual(logger.calls[0][0], 'ok');
});
test('échec attendu', () => assertEqual(2 + 2, 5));
console.log(`Résumé: ${passed} réussis, ${failed} échoué(s)`);
