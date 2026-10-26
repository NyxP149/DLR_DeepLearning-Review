export {};

const fr = { 'message.bonjour': 'Bonjour {name}', 'message.aurevoir': 'Au revoir {name}' };

class Translator<M extends Record<string, string>> {
  constructor(private readonly messages: M) {}

  t<K extends keyof M & string>(key: K, params: Record<string, string> = {}): string {
    const template = this.messages[key];
    if (template === undefined) throw new Error(`cle inconnue ${key}`);
    return template.replace(/\{(\w+)\}/g, (_, name: string) => params[name] ?? `{${name}}`);
  }
}

const translator = new Translator(fr);
console.log(translator.t('message.bonjour', { name: 'Ada' }));
console.log(translator.t('message.aurevoir', { name: 'Ada' }));
try {
  translator.t('message.merci' as keyof typeof fr);
} catch (error) {
  console.log(`Erreur: ${(error as Error).message}`);
}
