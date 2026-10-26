export {};

const imports = ['cart -> shared', 'cart -> catalog', 'shared -> cart', 'core -> shared', 'checkout -> core'];
const features = new Set(['catalog', 'cart', 'checkout']);

function verdict(source: string, target: string): string | null {
  if (source === 'core') return 'core ne depend de rien';
  if (source === 'shared' && features.has(target)) return 'shared ne depend d\'aucune fonctionnalite';
  if (features.has(source) && features.has(target)) return 'une fonctionnalite ne depend pas d\'une autre';
  return null;
}

let violations = 0;
for (const line of imports) {
  const [source, target] = line.split(' -> ');
  const problem = verdict(source, target);
  if (problem) violations += 1;
  console.log(`${line}: ${problem ? `INTERDIT (${problem})` : 'OK'}`);
}
console.log(`Violations: ${violations}`);
