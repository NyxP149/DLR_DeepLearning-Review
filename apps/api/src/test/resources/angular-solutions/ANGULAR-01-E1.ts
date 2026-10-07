type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-ARCHITECTURE', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-01: preuve validée');
