export {};

interface Settings {
  theme: string;
  fontSize: number;
  compact: boolean;
}

type Validators<T> = { [K in keyof T]: (value: T[K]) => boolean };

const base: Settings = { theme: 'light', fontSize: 12, compact: true };

function merge(current: Settings, patch: Partial<Settings>): Settings {
  return { ...current, ...patch };
}

function pickKeys<T, K extends keyof T>(source: T, keys: K[]): Pick<T, K> {
  const result = {} as Pick<T, K>;
  for (const key of keys) result[key] = source[key];
  return result;
}

function validate<T>(value: T, validators: Validators<T>): string[] {
  return (Object.keys(validators) as (keyof T)[])
    .filter((key) => !validators[key](value[key]))
    .map(String);
}

const updated = merge(base, { theme: 'dark', fontSize: 14 });
console.log(`merge: ${JSON.stringify(updated)}`);
console.log(`pick: ${JSON.stringify(pickKeys(updated, ['theme', 'fontSize']))}`);
const validators: Validators<Settings> = {
  theme: (theme) => theme.length > 0,
  fontSize: (size) => size >= 8 && size <= 12,
  compact: () => true,
};
console.log(`invalides: ${validate(updated, validators).join(', ')}`);
