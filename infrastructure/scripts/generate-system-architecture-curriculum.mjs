import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const directory = resolve(root, 'content/architecture');

const topics = [
  {
    title: 'Lire un système comme une chaîne de responsabilités', concept: 'SYSTEM-BOUNDARIES',
    objective: 'Identifier acteurs, responsabilités, données et frontières avant de choisir une technologie',
    starter: `system = {"client": "Angular", "api": "Spring Boot", "data": "PostgreSQL"}\nfor responsibility, component in system.items():\n    print(f"{responsibility} -> {component}")`,
    output: 'client -> Angular\napi -> Spring Boot\ndata -> PostgreSQL'
  },
  {
    title: 'Top-down : du besoin utilisateur aux contrats', concept: 'TOP-DOWN-DESIGN',
    objective: 'Transformer un parcours utilisateur en capacités, endpoints et critères vérifiables',
    starter: `story = "Créer une commande"\ncontract = {"method": "POST", "path": "/orders", "success": 201}\nprint("Besoin:", story)\nprint(f"Contrat: {contract['method']} {contract['path']} -> {contract['success']}")`,
    output: 'Besoin: Créer une commande\nContrat: POST /orders -> 201'
  },
  {
    title: 'Bottom-up : des données vers le produit', concept: 'BOTTOM-UP-DESIGN',
    objective: 'Partir des invariants de données pour construire repository, service, API et écran',
    starter: `layers = ["PostgreSQL", "Repository", "Service", "API REST", "Interface"]\nfor index, layer in enumerate(layers, 1):\n    print(f"{index}. {layer}")`,
    output: '1. PostgreSQL\n2. Repository\n3. Service\n4. API REST\n5. Interface'
  },
  {
    title: 'Choisir une stack verticale avec une matrice de décision', concept: 'STACK-DECISION',
    objective: 'Comparer des stacks selon les contraintes plutôt que selon les préférences',
    starter: `stacks = {\n    "Spring-Angular": {"team": 5, "speed": 3, "ops": 4},\n    "FastAPI-TypeScript": {"team": 3, "speed": 5, "ops": 3},\n}\nweights = {"team": 2, "speed": 1, "ops": 2}\nscores = {name: sum(values[key] * weights[key] for key in weights) for name, values in stacks.items()}\nwinner = max(scores, key=scores.get)\nprint("Choix:", winner)\nprint("Score:", scores[winner])`,
    output: 'Choix: Spring-Angular\nScore: 21'
  },
  {
    title: 'Modéliser les données et leurs invariants', concept: 'DATA-MODEL',
    objective: 'Définir identités, relations, contraintes et historique avant la persistance',
    starter: `order = {"id": 42, "customer_id": 7, "lines": [25, 15], "status": "DRAFT"}\nassert order["lines"] and all(price > 0 for price in order["lines"])\nprint("Total:", sum(order["lines"]))\nprint("Invariant: valide")`,
    output: 'Total: 40\nInvariant: valide'
  },
  {
    title: 'Concevoir API, erreurs et idempotence', concept: 'API-CONTRACT',
    objective: 'Rendre les commandes répétables et les erreurs compréhensibles par tous les clients',
    starter: `seen = set()\ndef create(key: str) -> tuple[int, str]:\n    if key in seen:\n        return 200, "rejouée"\n    seen.add(key)\n    return 201, "créée"\nprint(*create("cmd-42"))\nprint(*create("cmd-42"))`,
    output: '201 créée\n200 rejouée'
  },
  {
    title: 'Projet 1 : application CRUD verticale', concept: 'VERTICAL-SLICE-PROJECT', type: 'PROJECT', threshold: 80,
    objective: 'Assembler modèle, service, contrat HTTP et représentation UI dans une tranche livrable',
    starter: `rows = []\ndef post(name: str) -> dict:\n    item = {"id": len(rows) + 1, "name": name.strip()}\n    rows.append(item)\n    return item\ncreated = post("  Architecture DLR  ")\nprint(f"POST /items -> 201 #{created['id']}")\nprint("Vue:", rows[0]["name"])`,
    output: 'POST /items -> 201 #1\nVue: Architecture DLR'
  },
  {
    title: 'Authentification et autorisation de bout en bout', concept: 'IDENTITY-ACCESS',
    objective: 'Séparer identité, session, permissions et décisions métier',
    starter: `permissions = {"reader": {"read"}, "editor": {"read", "write"}}\ndef authorize(role: str, action: str) -> int:\n    return 200 if action in permissions.get(role, set()) else 403\nprint("reader/write:", authorize("reader", "write"))\nprint("editor/write:", authorize("editor", "write"))`,
    output: 'reader/write: 403\neditor/write: 200'
  },
  {
    title: 'Transactions, concurrence et cohérence', concept: 'CONSISTENCY',
    objective: 'Choisir une frontière transactionnelle et empêcher les mises à jour perdues',
    starter: `account = {"balance": 100, "version": 3}\ndef debit(amount: int, expected_version: int) -> str:\n    if expected_version != account["version"]:\n        return "CONFLIT"\n    account["balance"] -= amount\n    account["version"] += 1\n    return "OK"\nprint(debit(30, 3), account)\nprint(debit(20, 3), account)`,
    output: "OK {'balance': 70, 'version': 4}\nCONFLIT {'balance': 70, 'version': 4}"
  },
  {
    title: 'Valider toute la pyramide de tests', concept: 'TEST-STRATEGY',
    objective: 'Répartir les preuves entre tests unitaires, intégration, contrat et end-to-end',
    starter: `suite = {"unitaires": 42, "intégration": 12, "contrats": 6, "e2e": 3}\nprint("Tests:", sum(suite.values()))\nprint("Couverture:", " -> ".join(suite))`,
    output: 'Tests: 63\nCouverture: unitaires -> intégration -> contrats -> e2e'
  },
  {
    title: 'Conteneuriser la stack et ses dépendances', concept: 'CONTAINER-TOPOLOGY',
    objective: 'Définir services, réseaux, volumes, santé et ordre de disponibilité',
    starter: `services = {"web": ["api"], "api": ["db"], "db": []}\nhealthy = {"db"}\nfor name in ("db", "api", "web"):\n    if all(dep in healthy for dep in services[name]):\n        healthy.add(name)\n        print(name, "HEALTHY")`,
    output: 'db HEALTHY\napi HEALTHY\nweb HEALTHY'
  },
  {
    title: 'Déployer avec configuration et secrets séparés', concept: 'DEPLOYMENT-CONFIG',
    objective: 'Promouvoir le même artefact entre environnements sans embarquer de secret',
    starter: `artifact = "dlr-api:1.4.0"\nenvironments = {"staging": "stg-db", "production": "prod-db"}\nfor env, database in environments.items():\n    print(f"{env}: {artifact} -> {database}")\nprint("Secrets dans image: 0")`,
    output: 'staging: dlr-api:1.4.0 -> stg-db\nproduction: dlr-api:1.4.0 -> prod-db\nSecrets dans image: 0'
  },
  {
    title: 'Découper un monolithe modulaire', concept: 'MODULAR-MONOLITH',
    objective: 'Créer des modules métier cohésifs avant d’envisager des microservices',
    starter: `modules = {"catalogue": {"product"}, "commandes": {"order"}, "identité": {"user"}}\nowners = {entity: module for module, entities in modules.items() for entity in entities}\nprint("Propriétaire order:", owners["order"])\nprint("Modules:", len(modules))`,
    output: 'Propriétaire order: commandes\nModules: 3'
  },
  {
    title: 'Projet 2 : système modulaire sécurisé', concept: 'SECURE-MODULAR-PROJECT', type: 'PROJECT', threshold: 80,
    objective: 'Combiner modules, contrôle d’accès, transaction et audit dans une livraison testable',
    starter: `audit = []\ndef approve(role: str, order_id: int) -> str:\n    if role != "manager":\n        audit.append((order_id, "DENIED"))\n        return "403"\n    audit.append((order_id, "APPROVED"))\n    return "200"\nprint("Employé:", approve("employee", 42))\nprint("Manager:", approve("manager", 42))\nprint("Audit:", audit)`,
    output: "Employé: 403\nManager: 200\nAudit: [(42, 'DENIED'), (42, 'APPROVED')]"
  },
  {
    title: 'Mesurer avant d’optimiser', concept: 'PERFORMANCE-BUDGET',
    objective: 'Définir SLI, budgets de latence et mesures par couche',
    starter: `latencies = {"db": 35, "api": 20, "network": 45, "ui": 30}\ntotal = sum(latencies.values())\nbudget = 150\nprint("Latence totale:", total, "ms")\nprint("Budget restant:", budget - total, "ms")`,
    output: 'Latence totale: 130 ms\nBudget restant: 20 ms'
  },
  {
    title: 'Introduire un cache sans perdre la vérité', concept: 'CACHE-STRATEGY',
    objective: 'Choisir clé, durée de vie et stratégie d’invalidation autour de la source de vérité',
    starter: `cache = {"product:7": ("Clavier", 2)}\ndef tick(key: str):\n    value, ttl = cache[key]\n    ttl -= 1\n    if ttl == 0:\n        del cache[key]\n        return "MISS"\n    cache[key] = (value, ttl)\n    return "HIT"\nprint(tick("product:7"))\nprint(tick("product:7"))`,
    output: 'HIT\nMISS'
  },
  {
    title: 'Découpler avec événements et files de messages', concept: 'ASYNC-MESSAGING',
    objective: 'Modéliser publication, consommation, reprise et idempotence',
    starter: `events = ["order:42", "order:42", "order:43"]\nprocessed = set()\nfor event in events:\n    if event in processed:\n        print(event, "IGNORÉ")\n    else:\n        processed.add(event)\n        print(event, "TRAITÉ")`,
    output: 'order:42 TRAITÉ\norder:42 IGNORÉ\norder:43 TRAITÉ'
  },
  {
    title: 'Stocker et distribuer les fichiers', concept: 'OBJECT-STORAGE',
    objective: 'Séparer métadonnées, contenu binaire, contrôle d’accès et durée de conservation',
    starter: `file = {"key": "exports/42.pdf", "owner": 7, "sha256": "a1b2", "size": 2048}\nurl_ttl_seconds = 300\nprint("Objet:", file["key"])\nprint("Métadonnées:", file["sha256"], file["size"])\nprint("URL temporaire:", url_ttl_seconds, "s")`,
    output: 'Objet: exports/42.pdf\nMétadonnées: a1b2 2048\nURL temporaire: 300 s'
  },
  {
    title: 'Observer traces, métriques et journaux', concept: 'OBSERVABILITY',
    objective: 'Relier une requête utilisateur à ses signaux techniques et métier',
    starter: `trace = {"id": "abc-42", "spans": [18, 42, 25], "errors": 0}\nprint("Trace:", trace["id"])\nprint("Durée:", sum(trace["spans"]), "ms")\nprint("Statut:", "OK" if trace["errors"] == 0 else "ERROR")`,
    output: 'Trace: abc-42\nDurée: 85 ms\nStatut: OK'
  },
  {
    title: 'Sécuriser chaque frontière', concept: 'DEFENSE-IN-DEPTH',
    objective: 'Appliquer validation, moindre privilège, chiffrement et audit à chaque couche',
    starter: `controls = ["TLS", "validation", "RBAC", "secrets", "audit"]\nrequired = {"TLS", "validation", "RBAC", "secrets", "audit"}\nprint("Contrôles:", len(controls))\nprint("Prêt:", set(controls) >= required)`,
    output: 'Contrôles: 5\nPrêt: True'
  },
  {
    title: 'Concevoir la résilience et la reprise', concept: 'RESILIENCE',
    objective: 'Combiner délais, retries bornés, circuit breaker, sauvegarde et objectifs de reprise',
    starter: `attempts = [False, False, True]\nfor number, success in enumerate(attempts, 1):\n    print(f"Tentative {number}: {'OK' if success else 'RETRY'}")\n    if success:\n        break\nprint("RPO: 5 min | RTO: 30 min")`,
    output: 'Tentative 1: RETRY\nTentative 2: RETRY\nTentative 3: OK\nRPO: 5 min | RTO: 30 min'
  },
  {
    title: 'Dimensionner et faire évoluer la plateforme', concept: 'SCALABILITY',
    objective: 'Relier charge, capacité, saturation et stratégie de mise à l’échelle',
    starter: `requests_per_second = 720\ncapacity_per_instance = 250\ninstances = -(-requests_per_second // capacity_per_instance)\nheadroom = instances * capacity_per_instance - requests_per_second\nprint("Instances:", instances)\nprint("Marge:", headroom, "req/s")`,
    output: 'Instances: 3\nMarge: 30 req/s'
  },
  {
    title: 'Projet professionnel : plateforme multi-services observable', concept: 'DISTRIBUTED-PROJECT', type: 'PROJECT', threshold: 85,
    objective: 'Concevoir une plateforme avec frontières métier, événements, observabilité et déploiement progressif',
    starter: `services = {"gateway": "healthy", "orders": "healthy", "billing": "degraded"}\nrelease = {"version": "2.0", "traffic_percent": 10}\nready = all(state == "healthy" for state in services.values())\nprint("Canary:", release["traffic_percent"], "%")\nprint("Promotion:", "GO" if ready else "STOP")\nprint("Service à traiter:", next(name for name, state in services.items() if state != "healthy"))`,
    output: 'Canary: 10 %\nPromotion: STOP\nService à traiter: billing'
  },
  {
    title: 'Défi final : architecture complète sous contraintes', concept: 'ARCHITECTURE-CHALLENGE', type: 'CHALLENGE', threshold: 90,
    objective: 'Arbitrer coût, fiabilité, sécurité et délai puis défendre une architecture déployable',
    starter: `options = {\n    "monolithe_modulaire": {"delivery": 5, "reliability": 4, "cost": 5},\n    "microservices": {"delivery": 2, "reliability": 3, "cost": 2},\n}\nweights = {"delivery": 3, "reliability": 2, "cost": 2}\nscores = {name: sum(values[key] * weights[key] for key in weights) for name, values in options.items()}\nchoice = max(scores, key=scores.get)\nprint("Décision:", choice)\nprint("Score:", scores[choice])\nprint("Condition de réévaluation: limites mesurées")`,
    output: 'Décision: monolithe_modulaire\nScore: 33\nCondition de réévaluation: limites mesurées'
  }
];

const escapeSql = (value) => value.replaceAll("'", "''");
const slugify = (value) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

const labs = topics.map((topic, index) => {
  const number = index + 1;
  const code = `ARCHITECTURE-${String(number).padStart(2, '0')}`;
  const activityType = topic.type ?? 'LAB';
  const threshold = topic.threshold ?? (number >= 15 ? 80 : number >= 8 ? 75 : 70);
  const prerequisites = number === 1 ? [] : [`ARCHITECTURE-${String(number - 1).padStart(2, '0')}`];
  return {
    code, language: 'PYTHON', number, slug: slugify(topic.title), title: topic.title,
    difficulty: activityType === 'CHALLENGE' ? 'DEFI' : activityType === 'PROJECT' ? 'PROJET' : number >= 15 ? 'PROFESSIONNEL' : number >= 8 ? 'INTERMEDIAIRE' : 'BASE',
    threshold, activityType, prerequisites,
    objectives: [topic.objective, 'Justifier les compromis et nommer un risque opérationnel', 'Produire une preuve reproductible avant d’élargir la solution'],
    sections: [
      {
        title: 'Concept clé et décision', conceptCodes: [topic.concept],
        content: `${topic.title} relie les besoins, le logiciel, les données et l’exploitation. Commence par les contraintes observables, trace les responsabilités, puis choisis la solution la plus simple qui respecte les invariants.`
      },
      {
        title: activityType === 'PROJECT' ? 'Projet d’architecture' : activityType === 'CHALLENGE' ? 'Défi de synthèse' : 'Construction verticale',
        content: `La preuve ${code} modélise une décision réelle sous forme déterministe. Elle ne remplace pas un déploiement complet : elle rend l’hypothèse testable avant l’intégration Spring Boot, Angular, PostgreSQL et Docker.`
      },
      {
        title: 'Revue professionnelle',
        content: 'Documente le contexte, la décision, une option rejetée, les conséquences, le signal à surveiller et la condition qui imposerait de revoir le choix.'
      }
    ],
    keyConcepts: [{
      code: topic.concept, name: topic.title,
      definition: `${topic.title} est une décision de conception vérifiable qui relie une contrainte à des composants et à leurs responsabilités.`,
      whyExists: 'Une stack verticale échoue lorsque les frontières et les invariants restent implicites entre la base, le serveur, le client et le déploiement.',
      whyImportant: 'Une décision explicite peut être testée, observée et révisée ; une préférence technique isolée ne le peut pas.',
      minimalExample: `Le programme ${code} réduit le problème à un modèle exécutable et à une sortie contrôlée.`,
      commonMistake: 'Choisir un outil avant d’identifier le besoin, la charge, le risque et le propriétaire de la donnée.',
      masteryQuestion: `Quel compromis, quel signal et quelle condition de réévaluation associes-tu à « ${topic.title} » ?`,
      masteryProof: 'Exécuter la preuve, modifier une contrainte, expliquer le résultat et dessiner la place du composant dans la stack complète.'
    }],
    exercises: [{
      code: `${code}-E1`, title: topic.objective,
      statement: 'Exécute la preuve, conserve la sortie attendue, puis explique la décision, une alternative et le signal de production qui validerait ton choix.',
      starterCode: `${topic.starter}\n`, expectedOutput: topic.output
    }],
    quiz: [
      { code: `${code}-Q1`, type: 'SINGLE_CHOICE', prompt: 'Quelle démarche produit la décision la plus robuste ?', choices: ['Relier contraintes, responsabilités, preuve et observabilité', 'Choisir la technologie la plus populaire', 'Ajouter des services avant de mesurer'], correctChoice: 0, expectedKeywords: [] },
      { code: `${code}-Q2`, type: 'FREE_TEXT', prompt: 'Nomme un compromis et le signal qui permettrait de le réévaluer.', choices: [], correctChoice: null, expectedKeywords: ['compromis', 'mesure', 'risque'] }
    ],
    checklist: ['Je peux dessiner la circulation de la donnée', 'Je sais nommer les responsabilités de chaque couche', 'Je justifie une alternative rejetée', 'Je définis une mesure et une condition de réévaluation']
  };
});

await mkdir(directory, { recursive: true });
for (const lab of labs) {
  await writeFile(resolve(directory, `architecture-${String(lab.number).padStart(2, '0')}.json`), `${JSON.stringify(lab, null, 2)}\n`, 'utf8');
}

const rows = labs.map((lab) => `    ('${lab.code}', ${lab.number}, '${escapeSql(lab.slug)}', '${escapeSql(lab.title)}', '${lab.difficulty}', ${lab.threshold}, '${lab.activityType}')`).join(',\n');
const migration = `-- Generated from infrastructure/scripts/generate-system-architecture-curriculum.mjs.\ninsert into learning_path (language, position, status)\nselect 'ARCHITECTURE', 9, 'AVAILABLE'\nwhere not exists (select 1 from learning_path where language = 'ARCHITECTURE');\n\ninsert into lab (id, path_id, number, slug, title, difficulty, threshold, type)\nselect new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type\nfrom learning_path path\ncross join (values\n${rows}\n) as new_lab(id, number, slug, title, difficulty, threshold, type)\nwhere path.language = 'ARCHITECTURE';\n`;
await writeFile(resolve(root, 'apps/api/src/main/resources/db/migration/V17__system_architecture_path.sql'), migration, 'utf8');
console.log(`Generated ${labs.length} System Architecture labs and Flyway V17.`);
