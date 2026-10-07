type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-SIGNALS', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-02: preuve validée');
