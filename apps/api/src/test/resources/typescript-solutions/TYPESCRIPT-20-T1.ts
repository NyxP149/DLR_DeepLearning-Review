export {};

function isSafeFileName(name: string): boolean {
  return name.length <= 40 && /^[A-Za-z0-9._-]+$/.test(name) && !name.includes('..');
}

function isSafeColor(value: string): boolean {
  return /^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(value);
}

for (const name of ['rapport.pdf', '../etc/passwd', 'a/b.txt']) {
  console.log(`${name}: nom ${isSafeFileName(name) ? 'accepte' : 'refuse'}`);
}
for (const color of ['#ff00aa', 'red;background:url(x)']) {
  console.log(`${color}: couleur ${isSafeColor(color) ? 'acceptee' : 'refusee'}`);
}
