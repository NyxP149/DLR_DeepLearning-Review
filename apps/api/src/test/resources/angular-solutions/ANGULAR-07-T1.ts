export {};

type Request = { method: string; url: string };
type Handler = (request: Request) => string;

const calls: [string, string][] = [
  ['GET', '/api/users'],
  ['GET', '/api/users'],
  ['POST', '/api/users'],
  ['GET', '/api/users'],
];

let backendCalls = 0;
const backend: Handler = () => {
  backendCalls += 1;
  return `reseau (appel ${backendCalls})`;
};

function withCache(next: Handler): Handler {
  const cache = new Map<string, string>();
  return (request) => {
    if (request.method === 'GET' && cache.has(request.url)) return 'cache';
    const response = next(request);
    if (request.method === 'GET') cache.set(request.url, response);
    else cache.clear();
    return response;
  };
}

const client = withCache(backend);
for (const [method, url] of calls) console.log(`${method} ${url} -> ${client({ method, url })}`);
