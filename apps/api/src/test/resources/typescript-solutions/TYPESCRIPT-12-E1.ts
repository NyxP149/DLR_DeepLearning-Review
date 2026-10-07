export {};

function isPublicImport(specifier: string): boolean {
  if (specifier.startsWith('.')) {
    return true;
  }
  const segments = specifier.split('/');
  return segments.length === (specifier.startsWith('@') ? 2 : 1);
}

function findCycle(graph: Record<string, string[]>): string[] | null {
  const done = new Set<string>();

  function visit(name: string, stack: string[]): string[] | null {
    if (stack.includes(name)) {
      return [...stack.slice(stack.indexOf(name)), name];
    }
    if (done.has(name)) {
      return null;
    }
    for (const dependency of graph[name] ?? []) {
      const cycle = visit(dependency, [...stack, name]);
      if (cycle) {
        return cycle;
      }
    }
    done.add(name);
    return null;
  }

  for (const name of Object.keys(graph)) {
    const cycle = visit(name, []);
    if (cycle) {
      return cycle;
    }
  }
  return null;
}

for (const specifier of ['@dlr/core', '@dlr/core/src/internal/cache', './local', 'zod']) {
  console.log(`${specifier}: ${isPublicImport(specifier) ? 'autorisé' : 'interdit (import profond)'}`);
}

const graphs: Array<Record<string, string[]>> = [
  { app: ['lib', 'utils'], lib: ['utils'], utils: [] },
  { a: ['b'], b: ['a'] },
];
for (const graph of graphs) {
  const cycle = findCycle(graph);
  console.log(cycle ? `Cycle: ${cycle.join(' -> ')}` : 'Cycle: aucun');
}
