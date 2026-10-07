export {};

function split(value: string): string[];
function split(value: string[]): string[];
function split(value: string | string[]): string[] {
  return Array.isArray(value) ? value : value.split(',');
}

function sum(...values: number[]): number {
  return values.reduce((total, value) => total + value, 0);
}

function retryDelay(attempt: number, base: number = 100): number {
  return base * 2 ** attempt;
}

console.log(`split("a,b,c") = ${split('a,b,c').join('|')}`);
console.log(`split(["x","y"]) = ${split(['x', 'y']).join('|')}`);
console.log(`sum() = ${sum()}`);
console.log(`sum(1, 2, 3) = ${sum(1, 2, 3)}`);
console.log(`retryDelay(2) = ${retryDelay(2)}`);
console.log(`retryDelay(2, 50) = ${retryDelay(2, 50)}`);
