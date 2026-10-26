export {};

function toArray(value: string): string[];
function toArray(value: number[]): string[];
function toArray(value: string | number[]): string[] {
  return typeof value === 'string' ? value.split(';') : value.map(String);
}

function average(...values: number[]): number {
  return values.length === 0 ? 0 : values.reduce((total, value) => total + value, 0) / values.length;
}

function timeoutFor(attempt: number, base: number = 200, factor: number = 3): number {
  return base * factor ** attempt;
}

console.log(`toArray("a;b") = ${toArray('a;b').join('|')}`);
console.log(`toArray([1,2]) = ${toArray([1, 2]).join('|')}`);
console.log(`average() = ${average()}`);
console.log(`average(2, 4, 9) = ${average(2, 4, 9)}`);
console.log(`timeoutFor(2) = ${timeoutFor(2)}`);
console.log(`timeoutFor(2, 100, 2) = ${timeoutFor(2, 100, 2)}`);
