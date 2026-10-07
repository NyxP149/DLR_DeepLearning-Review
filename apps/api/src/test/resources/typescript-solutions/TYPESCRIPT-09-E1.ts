export {};

interface User {
  id: number;
  name: string;
  email: string;
}

type Flags<T> = { [K in keyof T]: boolean };

function update(user: User, patch: Partial<User>): User {
  return { ...user, ...patch };
}

function pick<T, K extends keyof T>(source: T, keys: K[]): Pick<T, K> {
  const result = {} as Pick<T, K>;
  for (const key of keys) {
    result[key] = source[key];
  }
  return result;
}

function toFlags<T extends object>(source: T): Flags<T> {
  const result = {} as Flags<T>;
  for (const key of Object.keys(source) as Array<keyof T>) {
    result[key] = Boolean(source[key]);
  }
  return result;
}

const user: User = { id: 1, name: 'Ada', email: '' };
console.log(`update: ${JSON.stringify(update(user, { email: 'ada@new.dev' }))}`);
console.log(`pick: ${JSON.stringify(pick(user, ['id', 'name']))}`);
console.log(`flags: ${JSON.stringify(toFlags(user))}`);
