export {};

const words = ['java', 'ts', 'python', 'go'];

class Queue<T> {
  private items: T[] = [];

  enqueue(item: T): void {
    this.items.push(item);
  }

  dequeue(): T | undefined {
    return this.items.shift();
  }

  get size(): number {
    return this.items.length;
  }
}

function maxBy<T>(items: readonly T[], key: (item: T) => number): T {
  return items.reduce((best, item) => (key(item) > key(best) ? item : best));
}

function countBy<T, K extends string>(items: readonly T[], classify: (item: T) => K): Record<string, number> {
  const counts: Record<string, number> = {};
  for (const item of items) {
    const category = classify(item);
    counts[category] = (counts[category] ?? 0) + 1;
  }
  return counts;
}

const queue = new Queue<string>();
words.forEach((word) => queue.enqueue(word));
console.log(`dequeue: ${queue.dequeue()}, taille restante: ${queue.size}`);
console.log(`plus long: ${maxBy(words, (word) => word.length)}`);
console.log(`groupes: ${JSON.stringify(countBy(words, (word) => (word.length > 3 ? 'long' : 'court')))}`);
