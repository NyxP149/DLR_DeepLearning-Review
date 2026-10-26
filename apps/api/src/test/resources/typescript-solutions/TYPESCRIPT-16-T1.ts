export {};

class TtlCache<V> {
  private entries = new Map<string, { value: V; expiresAt: number }>();

  constructor(private readonly clock: () => number, private readonly ttl: number) {}

  set(key: string, value: V): void {
    this.entries.set(key, { value, expiresAt: this.clock() + this.ttl });
  }

  get(key: string): V | undefined {
    const entry = this.entries.get(key);
    if (!entry || this.clock() >= entry.expiresAt) return undefined;
    return entry.value;
  }
}

let passed = 0;
let failed = 0;

function check(name: string, ok: boolean): void {
  if (ok) passed += 1;
  else failed += 1;
  console.log(`${ok ? '✓' : '✗'} ${name}`);
}

let now = 1000;
const cache = new TtlCache<string>(() => now, 500);
cache.set('a', 'valeur');
now += 499;
check('valeur conservee avant expiration', cache.get('a') === 'valeur');
now += 1;
check('valeur expiree apres le ttl', cache.get('a') === undefined);
check('cle inconnue renvoie undefined', cache.get('zzz') === undefined);
console.log(`Resume: ${passed} reussis, ${failed} echoue(s)`);
