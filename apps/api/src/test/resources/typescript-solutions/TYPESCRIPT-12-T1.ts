export {};

const healthy: Record<string, string[]> = { api: ['db', 'utils'], db: ['utils'], utils: [] };
const cyclic: Record<string, string[]> = { a: ['b'], b: ['c'], c: ['a'] };

function order(graph: Record<string, string[]>): string[] {
  const done = new Set<string>();
  const path: string[] = [];
  const result: string[] = [];

  const visit = (name: string): void => {
    if (done.has(name)) return;
    const position = path.indexOf(name);
    if (position >= 0) {
      throw new Error(`Cycle: ${[...path.slice(position), name].join(' -> ')}`);
    }
    path.push(name);
    for (const dependency of graph[name] ?? []) visit(dependency);
    path.pop();
    done.add(name);
    result.push(name);
  };

  Object.keys(graph).forEach(visit);
  return result;
}

console.log(`Ordre: ${order(healthy).join(' -> ')}`);
try {
  order(cyclic);
} catch (error) {
  console.log((error as Error).message);
}
