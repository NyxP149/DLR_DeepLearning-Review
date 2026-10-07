export {};

interface Element {
  tag: 'img' | 'button' | 'input';
  id: string;
  alt?: string;
  label?: string;
  text?: string;
}

const elements: Element[] = [
  { tag: 'img', id: 'logo' },
  { tag: 'img', id: 'avatar', alt: 'Photo de profil' },
  { tag: 'button', id: 'save' },
  { tag: 'button', id: 'close', text: 'Fermer' },
  { tag: 'input', id: 'email' },
  { tag: 'input', id: 'name', label: 'Nom' },
];

interface Bundle {
  file: string;
  kb: number;
  budget: number;
}

const bundles: Bundle[] = [
  { file: 'main.js', kb: 520, budget: 500 },
  { file: 'styles.css', kb: 80, budget: 100 },
];

function a11yProblem(element: Element): string | null {
  if (element.tag === 'img' && !element.alt) return 'alt manquant';
  if (element.tag === 'button' && !element.text && !element.label) return 'nom accessible manquant';
  if (element.tag === 'input' && !element.label) return 'label manquant';
  return null;
}

function budgetLine(bundle: Bundle): string {
  if (bundle.kb > bundle.budget) {
    return `Budget: ${bundle.file} ${bundle.kb} kB > ${bundle.budget} kB (dépassé de ${bundle.kb - bundle.budget} kB)`;
  }
  return `Budget: ${bundle.file} ${bundle.kb} kB <= ${bundle.budget} kB`;
}

let violations = 0;
for (const element of elements) {
  const problem = a11yProblem(element);
  if (problem) {
    violations++;
    console.log(`${element.tag}#${element.id}: ${problem}`);
  }
}
console.log(`Accessibilité: ${violations} violation(s)`);
for (const bundle of bundles) {
  console.log(budgetLine(bundle));
}
