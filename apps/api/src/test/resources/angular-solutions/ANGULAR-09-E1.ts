type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-PROJECT', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-09: preuve validée');
