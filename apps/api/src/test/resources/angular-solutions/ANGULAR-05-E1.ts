export {};

interface Route {
  path: string;
  component: string;
  module?: string;
  guard?: () => boolean;
}

const auth = { loggedIn: false };

const routes: Route[] = [
  { path: 'orders/:id', component: 'OrderDetail', module: 'orders' },
  { path: 'admin', component: 'AdminPage', module: 'admin', guard: () => auth.loggedIn },
  { path: 'login', component: 'LoginPage' },
];

const loaded: string[] = [];

function match(route: Route, url: string): Record<string, string> | null {
  const expected = route.path.split('/');
  const actual = url.split('/');
  if (expected.length !== actual.length) {
    return null;
  }
  const params: Record<string, string> = {};
  for (let index = 0; index < expected.length; index++) {
    if (expected[index].startsWith(':')) {
      params[expected[index].slice(1)] = actual[index];
    } else if (expected[index] !== actual[index]) {
      return null;
    }
  }
  return params;
}

function navigate(url: string): string {
  for (const route of routes) {
    const params = match(route, url);
    if (params === null) {
      continue;
    }
    if (route.guard && !route.guard()) {
      return 'redirection vers /login';
    }
    if (route.module && !loaded.includes(route.module)) {
      loaded.push(route.module);
    }
    const entries = Object.entries(params).map(([key, value]) => `${key}=${value}`);
    return entries.length ? `${route.component} (${entries.join(', ')})` : route.component;
  }
  return 'NotFound';
}

for (const url of ['orders/42', 'admin', 'login']) {
  console.log(`/${url} -> ${navigate(url)}`);
}
auth.loggedIn = true;
console.log(`/admin -> ${navigate('admin')}`);
console.log(`/inconnu -> ${navigate('inconnu')}`);
console.log(`Modules chargés: ${loaded.join(', ')}`);
