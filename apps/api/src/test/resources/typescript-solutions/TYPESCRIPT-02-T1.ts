export {};

function median(values: number[]): number | null {
  if (values.length === 0) return null;
  const sorted = [...values].sort((a, b) => a - b);
  return sorted[Math.floor(sorted.length / 2)];
}

function parsePercent(text: string): number | null {
  if (!/^-?\d+$/.test(text)) return null;
  const value = Number(text);
  return value >= 0 && value <= 100 ? value : null;
}

function firstWord(text: string): string | undefined {
  return text.trim().split(/\s+/)[0] || undefined;
}

console.log(`Mediane: ${median([5, 1, 3])}`);
console.log(`Mediane vide: ${median([]) ?? 'aucune donnee'}`);
for (const text of ['42', 'abc', '150']) {
  console.log(`Pourcentage "${text}": ${parsePercent(text) ?? 'invalide'}`);
}
console.log(`Premier mot: ${firstWord('bonjour le monde')?.toString() ?? 'aucun'}`);
console.log(`Premier mot vide: ${firstWord('')?.toString() ?? 'aucun'}`);
