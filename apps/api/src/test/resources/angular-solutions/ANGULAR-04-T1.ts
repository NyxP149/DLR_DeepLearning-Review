export {};

type Form = { code: string; password: string; confirmation: string };
type Validator = (value: string, form: Form) => string | null;

const submissions: Form[] = [
  { code: '', password: 'secret123', confirmation: 'autre' },
  { code: 'abc-1', password: 'secret123', confirmation: 'secret123' },
  { code: 'ABC-123', password: 'secret123', confirmation: 'secret123' },
];

const required: Validator = (value) => (value.trim() === '' ? 'obligatoire' : null);
const pattern = (regex: RegExp, message: string): Validator => (value) => (regex.test(value) ? null : message);
const sameAs = (other: keyof Form, message: string): Validator => (value, form) => (value === form[other] ? null : message);

function composeFirst(...validators: Validator[]): Validator {
  return (value, form) => {
    for (const validator of validators) {
      const error = validator(value, form);
      if (error) return error;
    }
    return null;
  };
}

const rules: Record<keyof Form, Validator> = {
  code: composeFirst(required, pattern(/^[A-Z]{3}-\d{3}$/, 'format invalide')),
  password: composeFirst(required),
  confirmation: composeFirst(required, sameAs('password', 'les mots de passe different')),
};

submissions.forEach((form, index) => {
  const errors = (Object.keys(rules) as (keyof Form)[])
    .map((field) => ({ field, error: rules[field](form[field], form) }))
    .filter((item) => item.error !== null)
    .map((item) => `${item.field}: ${item.error}`);
  console.log(`Soumission ${index + 1}: ${errors.length === 0 ? 'valide' : `invalide (${errors.join('; ')})`}`);
});
