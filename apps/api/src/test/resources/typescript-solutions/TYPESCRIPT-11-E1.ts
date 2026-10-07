export {};

type Result<T, E> = { ok: true; value: T } | { ok: false; error: E };

function parseQuantity(input: string): Result<number, string> {
  const value = Number(input);
  if (input.trim() === '' || Number.isNaN(value)) {
    return { ok: false, error: 'quantité illisible' };
  }
  if (value < 0) {
    return { ok: false, error: 'quantité négative' };
  }
  if (!Number.isInteger(value)) {
    return { ok: false, error: 'quantité non entière' };
  }
  return { ok: true, value };
}

function sumValid(results: Result<number, string>[]): number {
  let total = 0;
  for (const result of results) {
    if (result.ok) {
      total += result.value;
    }
  }
  return total;
}

const inputs = ['5', '-2', 'abc'];
const results = inputs.map(parseQuantity);
inputs.forEach((input, index) => {
  const result = results[index];
  console.log(result.ok ? `"${input}" -> ok ${result.value}` : `"${input}" -> erreur: ${result.error}`);
});
console.log(`Total: ${sumValid(results)} (sur ${inputs.length} entrées)`);
