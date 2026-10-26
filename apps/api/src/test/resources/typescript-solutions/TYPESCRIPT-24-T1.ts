export {};

interface Todo {
  id: number;
  title: string;
  done: boolean;
}

type Action =
  | { type: 'add'; title: string }
  | { type: 'toggle'; id: number }
  | { type: 'clearDone' };

function assertNever(value: never): never {
  throw new Error(`action non traitee: ${JSON.stringify(value)}`);
}

function reducer(state: readonly Todo[], action: Action): Todo[] {
  switch (action.type) {
    case 'add':
      return [...state, { id: state.length + 1, title: action.title, done: false }];
    case 'toggle':
      return state.map((todo) => (todo.id === action.id ? { ...todo, done: !todo.done } : todo));
    case 'clearDone':
      return state.filter((todo) => !todo.done);
    default:
      return assertNever(action);
  }
}

let state: Todo[] = [];
const actions: Action[] = [
  { type: 'add', title: 'ecrire' },
  { type: 'add', title: 'relire' },
  { type: 'toggle', id: 1 },
  { type: 'clearDone' },
];
for (const action of actions) {
  state = reducer(state, action);
  console.log(`${action.type}: ${JSON.stringify(state)}`);
}
