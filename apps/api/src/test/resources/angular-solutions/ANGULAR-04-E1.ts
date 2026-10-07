export {};

type Validator = (value: string) => string | null;

const required: Validator = (value) => (value.trim() === '' ? 'obligatoire' : null);

const email: Validator = (value) => (/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value) ? null : 'format invalide');

function minLength(min: number): Validator {
  return (value) => (value.length < min ? `${min} caractères minimum` : null);
}

function firstError(value: string, validators: Validator[]): string | null {
  for (const validator of validators) {
    const error = validator(value);
    if (error) {
      return error;
    }
  }
  return null;
}

const fields: Record<string, Validator[]> = {
  email: [required, email],
  password: [required, minLength(8)],
};

function submit(index: number, values: Record<string, string>): void {
  const errors: string[] = [];
  for (const [field, validators] of Object.entries(fields)) {
    const error = firstError(values[field] ?? '', validators);
    if (error) {
      errors.push(`${field}: ${error}`);
    }
  }
  console.log(errors.length ? `Soumission ${index}: invalide (${errors.join('; ')})` : `Soumission ${index}: valide`);
}

submit(1, { email: '', password: 'abc' });
submit(2, { email: 'ada-at-dlr', password: 'motdepasse1' });
submit(3, { email: 'ada@dlr.dev', password: 'motdepasse1' });
