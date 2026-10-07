export {};

interface Events {
  created: { id: number };
  deleted: { id: number };
}

class EventBus<E> {
  private handlers = new Map<keyof E, Array<(payload: any) => void>>();

  on<K extends keyof E>(event: K, handler: (payload: E[K]) => void): () => void {
    const list = this.handlers.get(event) ?? [];
    list.push(handler);
    this.handlers.set(event, list);
    return () => {
      const current = this.handlers.get(event) ?? [];
      this.handlers.set(event, current.filter((item) => item !== handler));
    };
  }

  emit<K extends keyof E>(event: K, payload: E[K]): void {
    for (const handler of this.handlers.get(event) ?? []) {
      handler(payload);
    }
  }
}

class TaskQueue {
  private chain: Promise<void> = Promise.resolve();

  push(task: () => Promise<void>): void {
    this.chain = this.chain.then(task);
  }

  idle(): Promise<void> {
    return this.chain;
  }
}

const sleep = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

async function main(): Promise<void> {
  const bus = new EventBus<Events>();
  let received = 0;
  const off = bus.on('created', (payload) => {
    received++;
    console.log(`reçu: created #${payload.id}`);
  });
  bus.emit('created', { id: 1 });
  bus.emit('created', { id: 2 });
  off();
  bus.emit('created', { id: 3 });
  console.log(`Appels reçus: ${received}`);

  const queue = new TaskQueue();
  for (const [name, delay] of [['tâche 1', 20], ['tâche 2', 5], ['tâche 3', 1]] as const) {
    queue.push(async () => {
      await sleep(delay);
      console.log(`${name} terminée`);
    });
  }
  await queue.idle();
}

main();
