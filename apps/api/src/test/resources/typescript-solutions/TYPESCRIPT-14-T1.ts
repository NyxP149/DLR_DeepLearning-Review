export {};

interface Product {
  sku: string;
  price: number;
}

const inputs = [
  '{"sku":"K-1","price":49.9}',
  '{"sku":"K-2","price":"cher"}',
  'pas du json',
  '{"price":10}',
];

function isProduct(value: unknown): value is Product {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as Record<string, unknown>)['sku'] === 'string' &&
    typeof (value as Record<string, unknown>)['price'] === 'number'
  );
}

function parseProduct(text: string): Product {
  let data: unknown;
  try {
    data = JSON.parse(text);
  } catch {
    throw new Error('JSON invalide');
  }
  if (!isProduct(data)) throw new Error('contrat viole');
  return data;
}

for (const text of inputs) {
  try {
    const product = parseProduct(text);
    console.log(`${text} -> Product ${product.sku}`);
  } catch (error) {
    console.log(`${text} -> rejete: ${(error as Error).message}`);
  }
}
