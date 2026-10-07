export {};

interface Contact {
  name: string;
  email?: string;
}

function label(contact: Contact): string {
  return `${contact.name} <${contact.email ?? 'inconnu'}>`;
}

class Robot {
  name: string;
  model: string;

  constructor(name: string, model: string) {
    this.name = name;
    this.model = model;
  }
}

const ada = { name: 'Ada', email: 'ada@dlr.dev' };
const linus = { name: 'Linus' };
const robot = new Robot('R2', 'astromech');

for (const contact of [ada, linus, robot]) {
  console.log(label(contact));
}
