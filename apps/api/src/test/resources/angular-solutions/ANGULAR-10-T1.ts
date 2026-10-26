export {};

interface Node { name: string; children: Node[]; }

const app: Node = {
  name: 'App',
  children: [
    { name: 'Header', children: [] },
    { name: 'List', children: [{ name: 'Item1', children: [] }, { name: 'Item2', children: [] }, { name: 'Item3', children: [] }] },
    { name: 'Footer', children: [] },
  ],
};

function dirtyPath(root: Node, target: string): Set<string> {
  const path = new Set<string>();
  const walk = (node: Node): boolean => {
    const inside = node.name === target || node.children.some(walk);
    if (inside) path.add(node.name);
    return inside;
  };
  walk(root);
  return path;
}

function checked(node: Node, onPush: boolean, dirty: Set<string>): number {
  if (onPush && !dirty.has(node.name)) return 0;
  return 1 + node.children.reduce((total, child) => total + checked(child, onPush, dirty), 0);
}

const dirty = dirtyPath(app, 'Item2');
const withDefault = checked(app, false, dirty);
const withOnPush = checked(app, true, dirty);
console.log(`Default: ${withDefault} composants verifies`);
console.log(`OnPush: ${withOnPush} composants verifies`);
console.log(`Gain: ${withDefault - withOnPush} verifications evitees`);
