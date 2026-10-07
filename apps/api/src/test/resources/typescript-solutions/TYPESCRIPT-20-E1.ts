export {};

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function isSafeRedirect(target: string): boolean {
  return target.startsWith('/') && !target.startsWith('//') && !target.startsWith('/\\');
}

console.log(escapeHtml('<script>alert(1)</script>'));
console.log(escapeHtml(`"x" & 'y'`));
for (const target of ['/dashboard', 'https://evil.example/phish', '//evil.example', '/\\evil.example']) {
  console.log(`${target}: redirection ${isSafeRedirect(target) ? 'autorisée' : 'refusée'}`);
}
