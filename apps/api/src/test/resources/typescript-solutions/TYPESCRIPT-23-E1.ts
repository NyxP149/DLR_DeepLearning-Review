export {};

interface User {
  id: number;
  name: string;
}
interface Order {
  id: number;
  total: number;
}
interface Routes {
  '/users/:id': User;
  '/orders/:id': Order;
}

type Transport = (url: string) => Promise<{ status: number; body: string }>;

function fill(path: string, params: Record<string, number | string>): string {
  return path.replace(/:([a-zA-Z]+)/g, (_match, key: string) => String(params[key]));
}

class Client {
  private transport: Transport;

  constructor(transport: Transport) {
    this.transport = transport;
  }

  async get<P extends keyof Routes>(path: P, params: Record<string, number | string>): Promise<Routes[P]> {
    const url = fill(path, params);
    const response = await this.transport(url);
    if (response.status !== 200) {
      throw new Error(`route ${url} introuvable`);
    }
    return JSON.parse(response.body) as Routes[P];
  }
}

const bodies: Record<string, string> = {
  '/users/1': '{"id":1,"name":"Ada"}',
  '/orders/7': '{"id":7,"total":120}',
};
const transport: Transport = async (url) =>
  url in bodies ? { status: 200, body: bodies[url] } : { status: 404, body: '' };

async function main(): Promise<void> {
  const client = new Client(transport);
  const user = await client.get('/users/:id', { id: 1 });
  console.log(`GET /users/1 -> ${user.name}`);
  const order = await client.get('/orders/:id', { id: 7 });
  console.log(`GET /orders/7 -> total ${order.total}`);
  try {
    await client.get('/users/:id', { id: 404 });
  } catch (error) {
    console.log(`Erreur: ${(error as Error).message}`);
  }
}

main();
