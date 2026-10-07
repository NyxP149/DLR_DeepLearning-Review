type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-RXJS', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-06: preuve validée');
