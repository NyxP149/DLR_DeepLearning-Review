type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-QUALITY', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-08: preuve validée');
