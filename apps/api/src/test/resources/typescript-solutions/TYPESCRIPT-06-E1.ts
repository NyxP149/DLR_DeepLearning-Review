export {};

function minMax(values: readonly number[]): [number, number] {
  return [Math.min(...values), Math.max(...values)];
}

function sortedCopy(values: readonly number[]): number[] {
  return [...values].sort((a, b) => a - b);
}

function distance(point: readonly [number, number]): number {
  return Math.hypot(point[0], point[1]);
}

const data: readonly number[] = [9, 2, 4];
const [min, max] = minMax(data);
console.log(`Min/Max: ${min} / ${max}`);
console.log(`Trié: ${sortedCopy(data).join(',')}`);
console.log(`Original inchangé: ${data.join(',')}`);
console.log(`Distance: ${distance([3, 4])}`);
