export {};

const values: readonly number[] = [7, 4, 9, 2];

function rangeOf(items: readonly number[]): [number, number] {
  return [Math.min(...items), Math.max(...items)];
}

function sortedDescending(items: readonly number[]): number[] {
  return [...items].sort((a, b) => b - a);
}

function midpoint(a: readonly [number, number], b: readonly [number, number]): [number, number] {
  return [(a[0] + b[0]) / 2, (a[1] + b[1]) / 2];
}

const [min, max] = rangeOf(values);
console.log(`Etendue: ${min} -> ${max}`);
console.log(`Decroissant: ${sortedDescending(values).join(',')}`);
console.log(`Original inchange: ${values.join(',')}`);
console.log(`Milieu: ${midpoint([0, 0], [4, 6]).join(',')}`);
