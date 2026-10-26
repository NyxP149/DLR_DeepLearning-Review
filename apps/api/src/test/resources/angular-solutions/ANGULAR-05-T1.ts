export {};

const catalog: Record<string, string> = { '7': 'Clavier' };
const navigation = ['/products/7', '/cart', '/login', '/cart', '/', '/nulle-part'];
let loggedIn = false;

function resolve(url: string): string {
  if (url === '/') return 'redirection vers /products';
  if (url === '/products') return 'ProductList';
  const detail = url.match(/^\/products\/([^/]+)$/);
  if (detail) {
    const name = catalog[detail[1]];
    return name ? `ProductDetail (id=${detail[1]}, resolu: ${name})` : 'NotFound';
  }
  if (url === '/login') {
    loggedIn = true;
    return 'LoginPage';
  }
  if (url === '/cart') return loggedIn ? 'CartPage' : 'redirection vers /login';
  return 'NotFound';
}

for (const url of navigation) console.log(`${url} -> ${resolve(url)}`);
