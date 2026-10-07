type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'TYPESCRIPT-NODE', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('TYPESCRIPT-15: preuve validée');
