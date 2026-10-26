export {};

interface Product {
  readonly name: string;
  price?: number;
}

class Gadget {
  constructor(readonly name: string) {}
}

function label(product: Product): string {
  return `${product.name} : ${product.price !== undefined ? `${product.price} EUR` : 'prix inconnu'}`;
}

type UserId = string & { readonly __brand: 'UserId' };

function asUserId(value: string): UserId {
  if (value.trim() === '') throw new Error('identifiant vide');
  return value as UserId;
}

function loadUser(id: UserId): string {
  return `Utilisateur ${id} charge`;
}

console.log(label({ name: 'Clavier', price: 49.9 }));
console.log(label({ name: 'Souris' }));
console.log(label(new Gadget('Gadget')));
console.log(loadUser(asUserId('u-1')));
try {
  asUserId(' ');
} catch (error) {
  console.log(`Refus: ${(error as Error).message}`);
}
