export {};

type UiEvent =
  | { kind: 'click'; x: number; y: number }
  | { kind: 'key'; key: string }
  | { kind: 'scroll'; delta: number };

function assertNever(value: never): never {
  throw new Error(`variante non traitee: ${JSON.stringify(value)}`);
}

function describe(event: UiEvent): string {
  switch (event.kind) {
    case 'click':
      return `click en (${event.x}, ${event.y})`;
    case 'key':
      return `touche ${event.key}`;
    case 'scroll':
      return `defilement de ${event.delta}`;
    default:
      return assertNever(event);
  }
}

const events: UiEvent[] = [
  { kind: 'click', x: 10, y: 20 },
  { kind: 'key', key: 'Enter' },
  { kind: 'scroll', delta: -120 },
];
events.forEach((event) => console.log(describe(event)));
