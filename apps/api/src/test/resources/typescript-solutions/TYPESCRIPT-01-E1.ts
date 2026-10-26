interface Named {
  name: string;
}

class Person {
  constructor(public name: string, public age: number) {}
}

class Robot {
  name = 'R2';
  model = 'astromech';
}

function describeLanguage(name: string): string {
  return `${name}: typage statique progressif`;
}

function greet(entity: Named): string {
  return `Bonjour ${entity.name}`;
}

function classify(value: unknown): string {
  if (typeof value === 'number') return 'nombre';
  if (typeof value === 'string') return 'texte';
  return 'autre';
}

console.log(describeLanguage('TypeScript'));
console.log([new Person('Ada', 36), new Robot(), { name: 'Linus' }].map(greet).join(' | '));
const typed: string = 'x';
console.log(`Type a l'execution: ${typeof typed}`);
console.log(`unknown: ${[classify(42), classify('x'), classify(true)].join(', ')}`);
