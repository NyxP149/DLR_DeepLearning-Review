type Evidence = Readonly<{ concept: string; validated: boolean }>;
const evidence: Evidence = { concept: 'ANGULAR-FORMS', validated: true };
if (!evidence.validated) throw new Error('preuve invalide');
console.log('ANGULAR-04: preuve validée');
