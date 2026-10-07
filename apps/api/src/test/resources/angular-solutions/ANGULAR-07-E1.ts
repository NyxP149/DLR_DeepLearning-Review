export {};

interface Req {
  method: string;
  url: string;
  headers: Record<string, string>;
}
interface Res {
  status: number;
}
type Handler = (req: Req) => Promise<Res>;
type Interceptor = (req: Req, next: Handler) => Promise<Res>;

function chain(interceptors: Interceptor[], backend: Handler): Handler {
  return interceptors.reduceRight<Handler>((next, interceptor) => (req) => interceptor(req, next), backend);
}

const withAuth = (token: string): Interceptor => async (req, next) => {
  return next({ ...req, headers: { ...req.headers, Authorization: `Bearer ${token}` } });
};

const withRetry = (max: number): Interceptor => async (req, next) => {
  let attempt = 0;
  let res: Res;
  do {
    attempt++;
    res = await next(req);
    console.log(`Tentative ${attempt}: ${res.status}`);
  } while (res.status === 503 && attempt < max);
  return res;
};

function userMessage(status: number): string {
  if (status === 401) return 'Session expirée';
  if (status === 404) return 'Ressource introuvable';
  if (status >= 500) return 'Service indisponible';
  return 'Erreur inconnue';
}

let calls = 0;
const backend: Handler = async (req) => {
  calls++;
  console.log(`Appel: ${req.method} ${req.url} auth=${req.headers['Authorization'] ?? 'aucune'}`);
  if (req.url === '/api/missing') {
    return { status: 404 };
  }
  return { status: calls < 2 ? 503 : 200 };
};

async function main(): Promise<void> {
  const http = chain([withAuth('t-42'), withRetry(3)], backend);
  for (const url of ['/api/orders', '/api/missing']) {
    const res = await http({ method: 'GET', url, headers: {} });
    console.log(res.status < 400 ? `Réponse: ${res.status}` : `Erreur ${res.status}: ${userMessage(res.status)}`);
  }
}

main();
