type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'TYPESCRIPT-SECURITY', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('TYPESCRIPT-20: preuve validée');
