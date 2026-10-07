export {};

type EventName<T extends string> = `${T}:created`;

type ElementOf<T> = T extends readonly (infer U)[] ? U : T;

function eventName<T extends string>(entity: T): EventName<T> {
  return `${entity}:created`;
}

function first<T>(value: T): ElementOf<T> {
  return (Array.isArray(value) ? value[0] : value) as ElementOf<T>;
}

const created: 'user:created' = eventName('user');
console.log(created);
console.log(eventName('order'));
console.log(`first([1, 2]) = ${first([1, 2])}`);
console.log(`first("x") = ${first('x')}`);
