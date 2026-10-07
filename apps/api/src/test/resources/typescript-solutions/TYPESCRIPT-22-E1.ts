type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'TYPESCRIPT-ANGULAR', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('TYPESCRIPT-22: preuve validée');
