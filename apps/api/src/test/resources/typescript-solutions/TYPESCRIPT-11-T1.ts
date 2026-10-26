export {};

type Result<T, E> = { ok: true; value: T } | { ok: false; error: E };

const inputs = ['12.5', '-3', 'abc'];

function parsePrice(text: string): Result<number, string> {
  const value = Number(text);
  if (text.trim() === '' || Number.isNaN(value)) return { ok: false, error: 'prix illisible' };
  if (value < 0) return { ok: false, error: 'prix negatif' };
  return { ok: true, value };
}

function sumValid(results: Result<number, string>[]): number {
  return results.reduce((total, result) => (result.ok ? total + result.value : total), 0);
}

const results = inputs.map(parsePrice);
results.forEach((result, index) => {
  console.log(`"${inputs[index]}" -> ${result.ok ? `ok ${result.value}` : `erreur: ${result.error}`}`);
});
console.log(`Total: ${sumValid(results)} (sur ${inputs.length} entrees)`);
