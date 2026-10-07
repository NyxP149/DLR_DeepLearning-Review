type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'TYPESCRIPT-TESTS', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('TYPESCRIPT-16: preuve validée');
