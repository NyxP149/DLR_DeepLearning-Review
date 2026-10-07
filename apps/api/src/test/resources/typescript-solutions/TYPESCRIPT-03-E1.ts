type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'TYPESCRIPT-NARROWING', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('TYPESCRIPT-03: preuve validée');
