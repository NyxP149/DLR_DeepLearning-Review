export {};

type Layer = 'feature' | 'shared' | 'core';

const layers: Record<string, Layer> = {
  orders: 'feature',
  customers: 'feature',
  shared: 'shared',
  core: 'core',
};

const imports: Array<[string, string]> = [
  ['orders', 'shared'],
  ['orders', 'customers'],
  ['shared', 'orders'],
  ['customers', 'core'],
];

function check(from: string, to: string): string | null {
  const source = layers[from];
  const target = layers[to];
  if (source === 'feature' && target === 'feature' && from !== to) {
    return "une fonctionnalité ne dépend pas d'une autre";
  }
  if (source === 'shared' && target === 'feature') {
    return "shared ne dépend d'aucune fonctionnalité";
  }
  if (source === 'core' && target !== 'core') {
    return "core ne dépend de rien d'autre";
  }
  return null;
}

let violations = 0;
for (const [from, to] of imports) {
  const problem = check(from, to);
  if (problem) {
    violations++;
    console.log(`${from} -> ${to}: INTERDIT (${problem})`);
  } else {
    console.log(`${from} -> ${to}: OK`);
  }
}
console.log(`Violations: ${violations}`);
