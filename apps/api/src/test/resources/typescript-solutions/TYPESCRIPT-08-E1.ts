export {};

class Stack<T> {
  private items: T[] = [];

  push(item: T): void {
    this.items.push(item);
  }

  pop(): T | undefined {
    return this.items.pop();
  }

  get size(): number {
    return this.items.length;
  }
}

function longest<T extends { length: number }>(a: T, b: T): T {
  return b.length > a.length ? b : a;
}

function groupBy<T>(items: T[], key: (item: T) => string): Record<string, T[]> {
  const groups: Record<string, T[]> = {};
  for (const item of items) {
    const name = key(item);
    (groups[name] ??= []).push(item);
  }
  return groups;
}

const stack = new Stack<number>();
stack.push(1);
stack.push(2);
stack.push(3);
console.log(`pop: ${stack.pop()}, taille restante: ${stack.size}`);
console.log(`plus long: ${longest('abc', 'abcd')}`);
console.log(`groupes: ${JSON.stringify(groupBy([1, 2, 3, 4], (n) => (n % 2 === 0 ? 'pair' : 'impair')))}`);
