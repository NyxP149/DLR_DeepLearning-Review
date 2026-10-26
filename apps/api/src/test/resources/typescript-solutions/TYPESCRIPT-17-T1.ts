export {};

const profile = Object.freeze({ name: 'Ada', tags: Object.freeze(['a']) });

function pipe<T>(...functions: ((value: T) => T)[]): (value: T) => T {
  return (value) => functions.reduce((current, next) => next(current), value);
}

function withTag(source: { readonly name: string; readonly tags: readonly string[] }, tag: string) {
  return { ...source, tags: [...source.tags, tag] };
}

const double = (n: number): number => n * 2;
const increment = (n: number): number => n + 1;
const copy = withTag(profile, 'b');
console.log(`pipe: ${pipe(double, increment)(5)}`);
console.log(`Original: ${JSON.stringify(profile)}`);
console.log(`Copie: ${JSON.stringify(copy)}`);
console.log(`Meme reference: ${(copy as unknown) === (profile as unknown)}`);
