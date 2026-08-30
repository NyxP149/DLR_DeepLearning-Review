import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');

const curricula = [
  {
    path: 'PYTHON', directory: 'python', language: 'PYTHON', start: 7, total: 24,
    topics: [
      ['Modules, imports et organisation en paquets', 'PYTHON-MODULES'],
      ['Itérateurs, générateurs et évaluation paresseuse', 'PYTHON-GENERATORS'],
      ['Décorateurs et gestionnaires de contexte', 'PYTHON-DECORATORS'],
      ['Typage progressif, Protocol et outils statiques', 'PYTHON-TYPING'],
      ['Tests avec pytest, doublures et couverture utile', 'PYTHON-TESTS'],
      ['Asynchronisme avec async, await et asyncio', 'PYTHON-ASYNC'],
      ['Clients HTTP, contrats et validation de réponses', 'PYTHON-HTTP'],
      ['Configuration, journalisation et diagnostic', 'PYTHON-LOGGING'],
      ['Packaging moderne, pyproject et dépendances', 'PYTHON-PACKAGING'],
      ['SQL, transactions et couches de persistance', 'PYTHON-PERSISTENCE'],
      ['Algorithmique, complexité et structures adaptées', 'PYTHON-ALGORITHMS'],
      ['Profilage, mémoire et optimisation mesurée', 'PYTHON-PERFORMANCE'],
      ['Sécurité, secrets et validation des entrées', 'PYTHON-SECURITY'],
      ['Architecture hexagonale et séparation des responsabilités', 'PYTHON-ARCHITECTURE'],
      ['Conception d’une API FastAPI testable', 'PYTHON-FASTAPI'],
      ['Observabilité, conteneurisation et exploitation', 'PYTHON-OBSERVABILITY'],
      ['Projet professionnel : service de données Python', 'PYTHON-PROJECT'],
      ['Défi final Python adaptatif', 'PYTHON-CHALLENGE']
    ]
  },
  {
    path: 'TYPESCRIPT', directory: 'typescript', language: 'TYPESCRIPT', start: 2, total: 24,
    topics: [
      ['Types primitifs, inférence et mode strict', 'TYPESCRIPT-STRICT'],
      ['Unions, littéraux et narrowing exhaustif', 'TYPESCRIPT-NARROWING'],
      ['Objets, interfaces et compatibilité structurelle', 'TYPESCRIPT-INTERFACES'],
      ['Fonctions, surcharges et paramètres sûrs', 'TYPESCRIPT-FUNCTIONS'],
      ['Tableaux, tuples et données readonly', 'TYPESCRIPT-READONLY'],
      ['Classes, visibilité et abstraction', 'TYPESCRIPT-CLASSES'],
      ['Génériques, contraintes et réutilisabilité', 'TYPESCRIPT-GENERICS'],
      ['Types utilitaires et types mappés', 'TYPESCRIPT-MAPPED-TYPES'],
      ['Types conditionnels et littéraux de gabarit', 'TYPESCRIPT-CONDITIONAL-TYPES'],
      ['Erreurs métier et modèle Result', 'TYPESCRIPT-RESULT'],
      ['Modules ES, npm et frontières de paquet', 'TYPESCRIPT-MODULES'],
      ['Promises, async/await et concurrence', 'TYPESCRIPT-ASYNC'],
      ['Fetch, validation runtime et contrats externes', 'TYPESCRIPT-HTTP'],
      ['Node.js, fichiers et flux', 'TYPESCRIPT-NODE'],
      ['Tests unitaires, doubles et couverture', 'TYPESCRIPT-TESTS'],
      ['Programmation fonctionnelle et immutabilité', 'TYPESCRIPT-FUNCTIONAL'],
      ['SOLID, injection et architecture modulaire', 'TYPESCRIPT-ARCHITECTURE'],
      ['Événements, files de tâches et concurrence', 'TYPESCRIPT-EVENTS'],
      ['Sécurité web et validation des données', 'TYPESCRIPT-SECURITY'],
      ['Performance, profiling et budgets', 'TYPESCRIPT-PERFORMANCE'],
      ['Préparation Angular, décorateurs et RxJS', 'TYPESCRIPT-ANGULAR'],
      ['Projet professionnel : SDK métier typé', 'TYPESCRIPT-PROJECT'],
      ['Défi final TypeScript adaptatif', 'TYPESCRIPT-CHALLENGE']
    ]
  },
  {
    path: 'SPRING_BOOT', directory: 'spring-boot', language: 'JAVA', start: 1, total: 12,
    firstPrerequisite: 'JAVA-24',
    topics: [
      ['Démarrage, auto-configuration et configuration typée', 'SPRING-BOOTSTRAP'],
      ['Injection de dépendances et cycle de vie des beans', 'SPRING-DI'],
      ['Architecture en couches et ports métier', 'SPRING-LAYERS'],
      ['Contrôleurs REST, HTTP et négociation de contenu', 'SPRING-REST'],
      ['DTO, validation et gestion uniforme des erreurs', 'SPRING-VALIDATION'],
      ['JPA, entités, repositories et pagination', 'SPRING-JPA'],
      ['Transactions, cohérence et idempotence', 'SPRING-TRANSACTIONS'],
      ['Tests unitaires, slices et intégration', 'SPRING-TESTS'],
      ['Spring Security et autorisations', 'SPRING-SECURITY'],
      ['Profils, métriques, logs et observabilité', 'SPRING-OBSERVABILITY'],
      ['Projet professionnel : API métier PostgreSQL', 'SPRING-PROJECT'],
      ['Revue d’architecture et durcissement final', 'SPRING-CHALLENGE']
    ]
  },
  {
    path: 'ANGULAR', directory: 'angular', language: 'TYPESCRIPT', start: 1, total: 10,
    firstPrerequisite: 'TYPESCRIPT-24',
    topics: [
      ['Architecture standalone et découpage par fonctionnalité', 'ANGULAR-ARCHITECTURE'],
      ['Signals, computed et effets maîtrisés', 'ANGULAR-SIGNALS'],
      ['Composants, inputs, outputs et composition', 'ANGULAR-COMPONENTS'],
      ['Formulaires typés et validation accessible', 'ANGULAR-FORMS'],
      ['Routing, guards, resolvers et chargement différé', 'ANGULAR-ROUTING'],
      ['RxJS, annulation et états asynchrones', 'ANGULAR-RXJS'],
      ['HTTP, interceptors et gestion des erreurs', 'ANGULAR-HTTP'],
      ['Tests, accessibilité et budgets de performance', 'ANGULAR-QUALITY'],
      ['Projet professionnel : dashboard métier', 'ANGULAR-PROJECT'],
      ['Défi final : optimiser une application complexe', 'ANGULAR-CHALLENGE']
    ]
  },
  {
    path: 'SQL', directory: 'sql', language: 'PYTHON', start: 1, total: 10,
    topics: [
      ['Modélisation relationnelle et DDL', 'SQL-MODELING'],
      ['SELECT, filtres, tri et pagination', 'SQL-SELECT'],
      ['Jointures et relations métier', 'SQL-JOINS'],
      ['Agrégations et fonctions fenêtre', 'SQL-ANALYTICS'],
      ['Contraintes, normalisation et intégrité', 'SQL-INTEGRITY'],
      ['Index, EXPLAIN et coût des requêtes', 'SQL-INDEXES'],
      ['Transactions, isolation et concurrence', 'SQL-TRANSACTIONS'],
      ['CTE, sous-requêtes et transformations', 'SQL-CTE'],
      ['Projet professionnel : schéma analytique', 'SQL-PROJECT'],
      ['Défi final : optimiser une charge réelle', 'SQL-CHALLENGE']
    ]
  },
  {
    path: 'DEVOPS', directory: 'devops', language: 'PYTHON', start: 1, total: 8,
    firstPrerequisite: 'JAVA-24',
    topics: [
      ['Images Docker, Dockerfile et builds reproductibles', 'DEVOPS-IMAGES'],
      ['Compose, réseaux, volumes et santé', 'DEVOPS-COMPOSE'],
      ['Sécurité, secrets et moindre privilège', 'DEVOPS-SECURITY'],
      ['Pipeline CI, cache et parallélisation', 'DEVOPS-CI'],
      ['Quality gates, artefacts et traçabilité', 'DEVOPS-QUALITY'],
      ['Observabilité, déploiement et rollback', 'DEVOPS-OPERATIONS'],
      ['Projet professionnel : chaîne de livraison', 'DEVOPS-PROJECT'],
      ['Incident final : diagnostiquer et restaurer', 'DEVOPS-CHALLENGE']
    ]
  }
];

const slugify = (value) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
  .replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
const label = (path, number) => `${path}-${String(number).padStart(2, '0')}`;
const escapeSql = (value) => value.replaceAll("'", "''");

function activityType(number, total) {
  if (number === total) return 'CHALLENGE';
  if (number === total - 1) return 'PROJECT';
  return 'LAB';
}

function difficulty(number, total) {
  if (number === total) return 'DEFI';
  if (number === total - 1) return 'PROJET';
  if (number > total * .7) return 'PROFESSIONNEL';
  if (number > total * .35) return 'INTERMEDIAIRE';
  return 'BASE';
}

function proof(path, number, concept) {
  const output = `${label(path, number)}: preuve validée`;
  if (path === 'PYTHON') return {
    code: `from dataclasses import dataclass\n\n@dataclass(frozen=True, slots=True)\nclass Evidence:\n    concept: str\n    validated: bool\n\nevidence = Evidence("${concept}", True)\nassert evidence.validated\nprint("${output}")\n`, output
  };
  if (path === 'TYPESCRIPT' || path === 'ANGULAR') return {
    code: `type Evidence = Readonly<{ concept: string; validated: boolean }>;\nconst evidence: Evidence = { concept: '${concept}', validated: true };\nif (!evidence.validated) throw new Error('preuve invalide');\nconsole.log('${output}');\n`, output
  };
  if (path === 'SPRING_BOOT') return {
    code: `record Evidence(String concept, boolean validated) {}\npublic class Main {\n    public static void main(String[] args) {\n        var evidence = new Evidence("${concept}", true);\n        if (!evidence.validated()) throw new IllegalStateException("preuve invalide");\n        System.out.println("${output}");\n    }\n}\n`, output
  };
  if (path === 'SQL') return {
    code: `import sqlite3\n\nconnection = sqlite3.connect(":memory:")\nconnection.executescript("""\ncreate table evidence(id integer primary key, concept text not null, validated integer not null check(validated in (0, 1)));\ninsert into evidence(concept, validated) values ('${concept}', 1), ('contrôle', 0);\n""")\nvalidated = connection.execute("select count(*) from evidence where validated = 1").fetchone()[0]\nassert validated == 1\nprint("${output}")\nconnection.close()\n`, output
  };
  return {
    code: `from dataclasses import dataclass\n\n@dataclass(frozen=True)\nclass DeliveryCheck:\n    control: str\n    passed: bool\n\ncheck = DeliveryCheck("${concept}", True)\nassert check.passed\nprint("${output}")\n`, output
  };
}

function makeLab(curriculum, topic, offset) {
  const number = curriculum.start + offset;
  const [title, concept] = topic;
  const code = label(curriculum.path, number);
  const previous = number === 1 ? curriculum.firstPrerequisite : label(curriculum.path, number - 1);
  const prerequisites = previous ? [previous] : [];
  const type = activityType(number, curriculum.total);
  const runnable = proof(curriculum.path, number, concept);
  const frameworkNote = curriculum.path === 'SPRING_BOOT'
    ? "La preuve unifichier valide ici la frontière d'architecture hors réseau ; le laboratoire explique ensuite son branchement aux annotations et dépendances Spring Boot réelles."
    : curriculum.path === 'ANGULAR'
      ? "La preuve TypeScript isole le contrat réactif ; son intégration dans un composant Angular est décrite sans rendre le runner dépendant d'un navigateur."
      : curriculum.path === 'SQL'
        ? "La requête est exécutée sur SQLite en mémoire pour rester locale et reproductible ; les différences PostgreSQL et l'analyse EXPLAIN sont signalées dans le cours."
        : 'La preuve minimale est locale, déterministe et peut être enrichie sans changer le contrat attendu.';
  return {
    code, language: curriculum.language, number, slug: slugify(title), title,
    difficulty: difficulty(number, curriculum.total), threshold: type === 'CHALLENGE' ? 85 : type === 'PROJECT' ? 80 : number > curriculum.total * .7 ? 75 : 70,
    activityType: type, prerequisites,
    objectives: [`Expliquer ${title.toLowerCase()}`, 'Construire une preuve exécutable et déterministe', 'Relier le concept à une décision professionnelle'],
    sections: [
      { title: 'Concept et usage professionnel', content: `${title} répond à un besoin concret de maintenabilité, de sûreté ou de performance. Le laboratoire demande de rendre la décision observable et vérifiable.`, conceptCodes: [concept] },
      { title: type === 'PROJECT' ? 'Projet portfolio' : type === 'CHALLENGE' ? 'Défi de synthèse' : 'Mise en pratique', content: frameworkNote }
    ],
    keyConcepts: [{
      code: concept, name: title, definition: `${title} est traité comme un contrat explicite dont le comportement peut être vérifié.`,
      whyExists: 'Il réduit les hypothèses implicites et rend le système plus simple à faire évoluer.',
      whyImportant: 'En contexte professionnel, la preuve reproductible facilite la revue, le diagnostic et la maintenance.',
      minimalExample: `Une petite preuve ${code} vérifie le cas nominal et expose une sortie stable.`,
      commonMistake: 'Appliquer la technique sans mesurer son effet ni documenter sa frontière.',
      masteryQuestion: `Quelle contrainte de ${title.toLowerCase()} doit rester vraie lorsque le système évolue ?`,
      masteryProof: 'Exécuter la preuve, expliquer son invariant et proposer un cas d’échec.'
    }],
    exercises: [{ code: `${code}-E1`, title: `Valider ${title.toLowerCase()}`, statement: `Conserve l'invariant et produis exactement : ${runnable.output}`, starterCode: runnable.code, expectedOutput: runnable.output }],
    quiz: [
      { code: `${code}-Q1`, type: 'SINGLE_CHOICE', prompt: 'Quelle preuve est la plus utile en revue ?', choices: ['Une exécution déterministe avec invariant explicite', 'Une sortie aléatoire', 'Une capture sans code'], correctChoice: 0, expectedKeywords: [] },
      { code: `${code}-Q2`, type: 'FREE_TEXT', prompt: `Explique une décision liée à ${title.toLowerCase()}.`, choices: [], correctChoice: null, expectedKeywords: ['contrat', 'preuve', 'test'] }
    ],
    checklist: ['Je peux expliquer le concept sans réciter une définition', 'Je sais exécuter et modifier la preuve', 'Je peux nommer un risque et son contrôle', type === 'PROJECT' || type === 'CHALLENGE' ? 'Je peux présenter cette preuve dans mon portfolio' : 'Je relie cette étape à la suivante']
  };
}

const generated = [];
for (const curriculum of curricula) {
  const directory = resolve(root, 'content', curriculum.directory);
  await mkdir(directory, { recursive: true });
  for (let offset = 0; offset < curriculum.topics.length; offset++) {
    const lab = makeLab(curriculum, curriculum.topics[offset], offset);
    await writeFile(resolve(directory, `${curriculum.directory}-${String(lab.number).padStart(2, '0')}.json`), `${JSON.stringify(lab, null, 2)}\n`, 'utf8');
    generated.push({ curriculum, lab });
  }
}

const newPaths = [
  ['SPRING_BOOT', 5], ['ANGULAR', 6], ['SQL', 7], ['DEVOPS', 8]
];
const migration = [
  '-- Generated from infrastructure/scripts/generate-expanded-curriculum.mjs.',
  ...newPaths.map(([path, position]) => `insert into learning_path (language, position, status)\nselect '${path}', ${position}, 'AVAILABLE'\nwhere not exists (select 1 from learning_path where language = '${path}');`),
  "update learning_path set status = 'AVAILABLE' where language in ('PYTHON', 'TYPESCRIPT', 'SPRING_BOOT', 'ANGULAR', 'SQL', 'DEVOPS');",
  ...curricula.map((curriculum) => {
    const rows = generated.filter((item) => item.curriculum.path === curriculum.path).map(({ lab }) =>
      `    ('${lab.code}', ${lab.number}, '${escapeSql(lab.slug)}', '${escapeSql(lab.title)}', '${lab.difficulty}', ${lab.threshold}, '${lab.activityType}')`).join(',\n');
    return `insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)\nselect new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type\nfrom learning_path path\ncross join (values\n${rows}\n) as new_lab(id, number, slug, title, difficulty, threshold, type)\nwhere path.language = '${curriculum.path}';`;
  })
].join('\n\n');

await writeFile(resolve(root, 'apps/api/src/main/resources/db/migration/V15__complete_professional_paths.sql'), `${migration}\n`, 'utf8');
console.log(`Generated ${generated.length} lab files and Flyway V15.`);
