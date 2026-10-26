export {};

interface Labeled { label: string; }

class Invoice { label = "Facture"; }
class Quote { label = "Devis"; }

function show(item: Labeled): string {
  return `Etiquette: ${item.label}`;
}

function classify(value: unknown): string {
  if (typeof value === 'number') return `nombre ${value}`;
  if (typeof value === 'string') return `texte ${value}`;
  return 'autre';
}

const amount: number = 42;
console.log(show(new Invoice()));
console.log(show(new Quote()));
console.log(show({ label: 'Avoir' }));
console.log(`Type a l'execution: ${typeof amount}`);
console.log(`unknown: ${[amount, 'dlr', null].map(classify).join(', ')}`);
