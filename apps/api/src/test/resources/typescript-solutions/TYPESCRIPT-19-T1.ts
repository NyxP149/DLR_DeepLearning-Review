export {};

interface EventMap {
  login: { user: string };
  logout: { user: string };
}

class EventBus<M> {
  private handlers: { [K in keyof M]?: ((payload: M[K]) => void)[] } = {};

  on<K extends keyof M>(name: K, handler: (payload: M[K]) => void): () => void {
    const list = this.handlers[name] ?? [];
    list.push(handler);
    this.handlers[name] = list;
    return () => {
      this.handlers[name] = (this.handlers[name] ?? []).filter((candidate) => candidate !== handler);
    };
  }

  emit<K extends keyof M>(name: K, payload: M[K]): void {
    (this.handlers[name] ?? []).forEach((handler) => handler(payload));
  }
}

const bus = new EventBus<EventMap>();
let extraCalls = 0;
let listening = false;
const offLogin = bus.on('login', ({ user }) => {
  console.log(`recu: login ${user}`);
  if (listening) extraCalls += 1;
});
bus.on('logout', ({ user }) => console.log(`recu: logout ${user}`));
bus.emit('login', { user: 'ada' });
bus.emit('logout', { user: 'ada' });
offLogin();
listening = true;
bus.emit('login', { user: 'ada' });
console.log(`Apres desabonnement: ${extraCalls} appel(s) supplementaire(s)`);
