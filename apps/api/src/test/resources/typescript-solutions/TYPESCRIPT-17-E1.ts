export {};

function pipe<T>(...fns: Array<(value: T) => T>): (value: T) => T {
  return (value) => fns.reduce((current, fn) => fn(current), value);
}

interface State {
  readonly count: number;
  readonly tags: readonly string[];
}

function addTag(state: State, tag: string): State {
  return { count: state.count + 1, tags: [...state.tags, tag] };
}

const add = (n: number) => (x: number) => x + n;
const double = (x: number) => x * 2;
console.log(`pipe: ${pipe(add(2), double, add(2))(3)}`);

const original: State = Object.freeze({ count: 1, tags: Object.freeze(['a']) });
const updated = addTag(original, 'b');
console.log(`Original: ${JSON.stringify(original)}`);
console.log(`Copie: ${JSON.stringify(updated)}`);
console.log(`Même référence: ${original === updated}`);
