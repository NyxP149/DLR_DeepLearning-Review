type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-COMPONENTS', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-03: preuve validée');
