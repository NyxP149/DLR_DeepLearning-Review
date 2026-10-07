type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-HTTP', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-07: preuve validée');
