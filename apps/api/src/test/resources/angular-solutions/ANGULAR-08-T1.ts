export {};

type Element = { tag: string; id: string; text?: string; caption?: string };

const elements: Element[] = [
  { tag: 'h1', id: 'title', text: 'Accueil' },
  { tag: 'a', id: 'more', text: 'cliquez ici' },
  { tag: 'table', id: 'sales', caption: '' },
  { tag: 'h4', id: 'detail', text: 'Détail' },
  { tag: 'a', id: 'docs', text: 'Lire la documentation' },
];
const bundles = [{ name: 'main.js', kb: 120, budget: 150 }, { name: 'vendor.js', kb: 410, budget: 350 }];

const violations: string[] = [];
let lastLevel = 0;
for (const element of elements) {
  if (element.tag === 'a' && (!element.text || element.text.trim().toLowerCase() === 'cliquez ici')) {
    violations.push(`a#${element.id}: texte de lien non explicite`);
  }
  if (element.tag === 'table' && !element.caption) {
    violations.push(`table#${element.id}: legende manquante`);
  }
  const heading = element.tag.match(/^h([1-6])$/);
  if (heading) {
    const level = Number(heading[1]);
    if (lastLevel > 0 && level > lastLevel + 1) {
      violations.push(`${element.tag}#${element.id}: niveau de titre saute (h${lastLevel} -> h${level})`);
    }
    lastLevel = level;
  }
}
violations.forEach((line) => console.log(line));
console.log(`Accessibilite: ${violations.length} violation(s)`);
for (const bundle of bundles) {
  const over = bundle.kb - bundle.budget;
  console.log(over > 0
    ? `Budget: ${bundle.name} ${bundle.kb} kB > ${bundle.budget} kB (depasse de ${over} kB)`
    : `Budget: ${bundle.name} ${bundle.kb} kB <= ${bundle.budget} kB OK`);
}
