export {};

interface Cart {
  items: string[];
  total: number;
}

type Action =
  | { type: 'add'; item: string; price: number }
  | { type: 'remove'; item: string; price: number }
  | { type: 'clear' };

function assertNever(value: never): never {
  throw new Error(`action inconnue: ${JSON.stringify(value)}`);
}

function reduce(state: Cart, action: Action): Cart {
  switch (action.type) {
    case 'add':
      return { items: [...state.items, action.item], total: state.total + action.price };
    case 'remove': {
      const index = state.items.indexOf(action.item);
      if (index === -1) {
        return state;
      }
      return {
        items: [...state.items.slice(0, index), ...state.items.slice(index + 1)],
        total: state.total - action.price,
      };
    }
    case 'clear':
      return { items: [], total: 0 };
    default:
      return assertNever(action);
  }
}

let state: Cart = { items: [], total: 0 };
const actions: Action[] = [
  { type: 'add', item: 'clavier', price: 50 },
  { type: 'add', item: 'souris', price: 20 },
  { type: 'remove', item: 'clavier', price: 50 },
  { type: 'remove', item: 'écran', price: 200 },
  { type: 'clear' },
];
for (const action of actions) {
  state = reduce(state, action);
  console.log(`${action.type}: ${JSON.stringify(state)}`);
}
