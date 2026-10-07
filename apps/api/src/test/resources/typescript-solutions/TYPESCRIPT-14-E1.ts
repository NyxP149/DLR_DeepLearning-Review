export {};

interface User {
  id: number;
  name: string;
}

function isUser(value: unknown): value is User {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return typeof candidate.id === 'number' && typeof candidate.name === 'string';
}

function parseUser(json: string): User {
  let data: unknown;
  try {
    data = JSON.parse(json);
  } catch {
    throw new Error('JSON invalide');
  }
  if (!isUser(data)) {
    throw new Error('contrat violé');
  }
  return data;
}

for (const input of ['{"id":1,"name":"Ada"}', '{"id":"1","name":"Ada"}', 'not json', '{"id":2}']) {
  try {
    console.log(`${input} -> User ${parseUser(input).name}`);
  } catch (error) {
    console.log(`${input} -> rejeté: ${(error as Error).message}`);
  }
}
