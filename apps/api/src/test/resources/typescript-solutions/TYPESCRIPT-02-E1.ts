export {};

function average(values: number[]): number | null {
  if (values.length === 0) {
    return null;
  }
  return values.reduce((sum, value) => sum + value, 0) / values.length;
}

function parseAge(input: string): number | null {
  if (!/^[0-9]+$/.test(input)) {
    return null;
  }
  const age = Number(input);
  return age <= 150 ? age : null;
}

console.log(`Moyenne: ${average([10, 20, 30]) ?? 'aucune donnée'}`);
console.log(`Moyenne vide: ${average([]) ?? 'aucune donnée'}`);
for (const input of ['42', 'abc', '-3', '200']) {
  const age = parseAge(input);
  console.log(`Âge "${input}": ${age === null ? 'invalide' : age}`);
}
