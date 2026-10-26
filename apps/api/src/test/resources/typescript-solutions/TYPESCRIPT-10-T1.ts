export {};

type RouteName<T extends string> = `/${T}`;
type Unwrap<T> = T extends Promise<infer U> ? U : T;

function route<T extends string>(name: T): RouteName<T> {
  return `/${name}`;
}

async function main(): Promise<void> {
  const users: '/users' = route('users');
  const orders: '/orders' = route('orders');
  const resolved: Unwrap<Promise<number>> = await Promise.resolve(42);
  const plain: Unwrap<string> = 'abc';
  console.log(users);
  console.log(orders);
  console.log(`promesse: ${resolved}`);
  console.log(`valeur: ${plain}`);
}

main();
