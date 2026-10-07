type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-ROUTING', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-05: preuve validée');
