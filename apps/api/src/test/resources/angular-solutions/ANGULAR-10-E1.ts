type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-CHALLENGE', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-10: preuve validée');
