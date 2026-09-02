-- Generated from infrastructure/scripts/generate-expanded-curriculum.mjs.

insert into learning_path (language, position, status)
select 'SPRING_BOOT', 5, 'AVAILABLE'
where not exists (select 1 from learning_path where language = 'SPRING_BOOT');

insert into learning_path (language, position, status)
select 'ANGULAR', 6, 'AVAILABLE'
where not exists (select 1 from learning_path where language = 'ANGULAR');

insert into learning_path (language, position, status)
select 'SQL', 7, 'AVAILABLE'
where not exists (select 1 from learning_path where language = 'SQL');

insert into learning_path (language, position, status)
select 'DEVOPS', 8, 'AVAILABLE'
where not exists (select 1 from learning_path where language = 'DEVOPS');

update learning_path set status = 'AVAILABLE' where language in ('PYTHON', 'TYPESCRIPT', 'SPRING_BOOT', 'ANGULAR', 'SQL', 'DEVOPS');

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('PYTHON-07', 7, 'modules-imports-et-organisation-en-paquets', 'Modules, imports et organisation en paquets', 'BASE', 70, 'LAB'),
    ('PYTHON-08', 8, 'iterateurs-generateurs-et-evaluation-paresseuse', 'Itérateurs, générateurs et évaluation paresseuse', 'BASE', 70, 'LAB'),
    ('PYTHON-09', 9, 'decorateurs-et-gestionnaires-de-contexte', 'Décorateurs et gestionnaires de contexte', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-10', 10, 'typage-progressif-protocol-et-outils-statiques', 'Typage progressif, Protocol et outils statiques', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-11', 11, 'tests-avec-pytest-doublures-et-couverture-utile', 'Tests avec pytest, doublures et couverture utile', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-12', 12, 'asynchronisme-avec-async-await-et-asyncio', 'Asynchronisme avec async, await et asyncio', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-13', 13, 'clients-http-contrats-et-validation-de-reponses', 'Clients HTTP, contrats et validation de réponses', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-14', 14, 'configuration-journalisation-et-diagnostic', 'Configuration, journalisation et diagnostic', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-15', 15, 'packaging-moderne-pyproject-et-dependances', 'Packaging moderne, pyproject et dépendances', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-16', 16, 'sql-transactions-et-couches-de-persistance', 'SQL, transactions et couches de persistance', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-17', 17, 'algorithmique-complexite-et-structures-adaptees', 'Algorithmique, complexité et structures adaptées', 'PROFESSIONNEL', 75, 'LAB'),
    ('PYTHON-18', 18, 'profilage-memoire-et-optimisation-mesuree', 'Profilage, mémoire et optimisation mesurée', 'PROFESSIONNEL', 75, 'LAB'),
    ('PYTHON-19', 19, 'securite-secrets-et-validation-des-entrees', 'Sécurité, secrets et validation des entrées', 'PROFESSIONNEL', 75, 'LAB'),
    ('PYTHON-20', 20, 'architecture-hexagonale-et-separation-des-responsabilites', 'Architecture hexagonale et séparation des responsabilités', 'PROFESSIONNEL', 75, 'LAB'),
    ('PYTHON-21', 21, 'conception-d-une-api-fastapi-testable', 'Conception d’une API FastAPI testable', 'PROFESSIONNEL', 75, 'LAB'),
    ('PYTHON-22', 22, 'observabilite-conteneurisation-et-exploitation', 'Observabilité, conteneurisation et exploitation', 'PROFESSIONNEL', 75, 'LAB'),
    ('PYTHON-23', 23, 'projet-professionnel-service-de-donnees-python', 'Projet professionnel : service de données Python', 'PROJET', 80, 'PROJECT'),
    ('PYTHON-24', 24, 'defi-final-python-adaptatif', 'Défi final Python adaptatif', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'PYTHON';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('TYPESCRIPT-02', 2, 'types-primitifs-inference-et-mode-strict', 'Types primitifs, inférence et mode strict', 'BASE', 70, 'LAB'),
    ('TYPESCRIPT-03', 3, 'unions-litteraux-et-narrowing-exhaustif', 'Unions, littéraux et narrowing exhaustif', 'BASE', 70, 'LAB'),
    ('TYPESCRIPT-04', 4, 'objets-interfaces-et-compatibilite-structurelle', 'Objets, interfaces et compatibilité structurelle', 'BASE', 70, 'LAB'),
    ('TYPESCRIPT-05', 5, 'fonctions-surcharges-et-parametres-surs', 'Fonctions, surcharges et paramètres sûrs', 'BASE', 70, 'LAB'),
    ('TYPESCRIPT-06', 6, 'tableaux-tuples-et-donnees-readonly', 'Tableaux, tuples et données readonly', 'BASE', 70, 'LAB'),
    ('TYPESCRIPT-07', 7, 'classes-visibilite-et-abstraction', 'Classes, visibilité et abstraction', 'BASE', 70, 'LAB'),
    ('TYPESCRIPT-08', 8, 'generiques-contraintes-et-reutilisabilite', 'Génériques, contraintes et réutilisabilité', 'BASE', 70, 'LAB'),
    ('TYPESCRIPT-09', 9, 'types-utilitaires-et-types-mappes', 'Types utilitaires et types mappés', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-10', 10, 'types-conditionnels-et-litteraux-de-gabarit', 'Types conditionnels et littéraux de gabarit', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-11', 11, 'erreurs-metier-et-modele-result', 'Erreurs métier et modèle Result', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-12', 12, 'modules-es-npm-et-frontieres-de-paquet', 'Modules ES, npm et frontières de paquet', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-13', 13, 'promises-async-await-et-concurrence', 'Promises, async/await et concurrence', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-14', 14, 'fetch-validation-runtime-et-contrats-externes', 'Fetch, validation runtime et contrats externes', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-15', 15, 'node-js-fichiers-et-flux', 'Node.js, fichiers et flux', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-16', 16, 'tests-unitaires-doubles-et-couverture', 'Tests unitaires, doubles et couverture', 'INTERMEDIAIRE', 70, 'LAB'),
    ('TYPESCRIPT-17', 17, 'programmation-fonctionnelle-et-immutabilite', 'Programmation fonctionnelle et immutabilité', 'PROFESSIONNEL', 75, 'LAB'),
    ('TYPESCRIPT-18', 18, 'solid-injection-et-architecture-modulaire', 'SOLID, injection et architecture modulaire', 'PROFESSIONNEL', 75, 'LAB'),
    ('TYPESCRIPT-19', 19, 'evenements-files-de-taches-et-concurrence', 'Événements, files de tâches et concurrence', 'PROFESSIONNEL', 75, 'LAB'),
    ('TYPESCRIPT-20', 20, 'securite-web-et-validation-des-donnees', 'Sécurité web et validation des données', 'PROFESSIONNEL', 75, 'LAB'),
    ('TYPESCRIPT-21', 21, 'performance-profiling-et-budgets', 'Performance, profiling et budgets', 'PROFESSIONNEL', 75, 'LAB'),
    ('TYPESCRIPT-22', 22, 'preparation-angular-decorateurs-et-rxjs', 'Préparation Angular, décorateurs et RxJS', 'PROFESSIONNEL', 75, 'LAB'),
    ('TYPESCRIPT-23', 23, 'projet-professionnel-sdk-metier-type', 'Projet professionnel : SDK métier typé', 'PROJET', 80, 'PROJECT'),
    ('TYPESCRIPT-24', 24, 'defi-final-typescript-adaptatif', 'Défi final TypeScript adaptatif', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'TYPESCRIPT';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('SPRING_BOOT-01', 1, 'demarrage-auto-configuration-et-configuration-typee', 'Démarrage, auto-configuration et configuration typée', 'BASE', 70, 'LAB'),
    ('SPRING_BOOT-02', 2, 'injection-de-dependances-et-cycle-de-vie-des-beans', 'Injection de dépendances et cycle de vie des beans', 'BASE', 70, 'LAB'),
    ('SPRING_BOOT-03', 3, 'architecture-en-couches-et-ports-metier', 'Architecture en couches et ports métier', 'BASE', 70, 'LAB'),
    ('SPRING_BOOT-04', 4, 'controleurs-rest-http-et-negociation-de-contenu', 'Contrôleurs REST, HTTP et négociation de contenu', 'BASE', 70, 'LAB'),
    ('SPRING_BOOT-05', 5, 'dto-validation-et-gestion-uniforme-des-erreurs', 'DTO, validation et gestion uniforme des erreurs', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SPRING_BOOT-06', 6, 'jpa-entites-repositories-et-pagination', 'JPA, entités, repositories et pagination', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SPRING_BOOT-07', 7, 'transactions-coherence-et-idempotence', 'Transactions, cohérence et idempotence', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SPRING_BOOT-08', 8, 'tests-unitaires-slices-et-integration', 'Tests unitaires, slices et intégration', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SPRING_BOOT-09', 9, 'spring-security-et-autorisations', 'Spring Security et autorisations', 'PROFESSIONNEL', 75, 'LAB'),
    ('SPRING_BOOT-10', 10, 'profils-metriques-logs-et-observabilite', 'Profils, métriques, logs et observabilité', 'PROFESSIONNEL', 75, 'LAB'),
    ('SPRING_BOOT-11', 11, 'projet-professionnel-api-metier-postgresql', 'Projet professionnel : API métier PostgreSQL', 'PROJET', 80, 'PROJECT'),
    ('SPRING_BOOT-12', 12, 'revue-d-architecture-et-durcissement-final', 'Revue d’architecture et durcissement final', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'SPRING_BOOT';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('ANGULAR-01', 1, 'architecture-standalone-et-decoupage-par-fonctionnalite', 'Architecture standalone et découpage par fonctionnalité', 'BASE', 70, 'LAB'),
    ('ANGULAR-02', 2, 'signals-computed-et-effets-maitrises', 'Signals, computed et effets maîtrisés', 'BASE', 70, 'LAB'),
    ('ANGULAR-03', 3, 'composants-inputs-outputs-et-composition', 'Composants, inputs, outputs et composition', 'BASE', 70, 'LAB'),
    ('ANGULAR-04', 4, 'formulaires-types-et-validation-accessible', 'Formulaires typés et validation accessible', 'INTERMEDIAIRE', 70, 'LAB'),
    ('ANGULAR-05', 5, 'routing-guards-resolvers-et-chargement-differe', 'Routing, guards, resolvers et chargement différé', 'INTERMEDIAIRE', 70, 'LAB'),
    ('ANGULAR-06', 6, 'rxjs-annulation-et-etats-asynchrones', 'RxJS, annulation et états asynchrones', 'INTERMEDIAIRE', 70, 'LAB'),
    ('ANGULAR-07', 7, 'http-interceptors-et-gestion-des-erreurs', 'HTTP, interceptors et gestion des erreurs', 'INTERMEDIAIRE', 70, 'LAB'),
    ('ANGULAR-08', 8, 'tests-accessibilite-et-budgets-de-performance', 'Tests, accessibilité et budgets de performance', 'PROFESSIONNEL', 75, 'LAB'),
    ('ANGULAR-09', 9, 'projet-professionnel-dashboard-metier', 'Projet professionnel : dashboard métier', 'PROJET', 80, 'PROJECT'),
    ('ANGULAR-10', 10, 'defi-final-optimiser-une-application-complexe', 'Défi final : optimiser une application complexe', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'ANGULAR';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('SQL-01', 1, 'modelisation-relationnelle-et-ddl', 'Modélisation relationnelle et DDL', 'BASE', 70, 'LAB'),
    ('SQL-02', 2, 'select-filtres-tri-et-pagination', 'SELECT, filtres, tri et pagination', 'BASE', 70, 'LAB'),
    ('SQL-03', 3, 'jointures-et-relations-metier', 'Jointures et relations métier', 'BASE', 70, 'LAB'),
    ('SQL-04', 4, 'agregations-et-fonctions-fenetre', 'Agrégations et fonctions fenêtre', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SQL-05', 5, 'contraintes-normalisation-et-integrite', 'Contraintes, normalisation et intégrité', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SQL-06', 6, 'index-explain-et-cout-des-requetes', 'Index, EXPLAIN et coût des requêtes', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SQL-07', 7, 'transactions-isolation-et-concurrence', 'Transactions, isolation et concurrence', 'INTERMEDIAIRE', 70, 'LAB'),
    ('SQL-08', 8, 'cte-sous-requetes-et-transformations', 'CTE, sous-requêtes et transformations', 'PROFESSIONNEL', 75, 'LAB'),
    ('SQL-09', 9, 'projet-professionnel-schema-analytique', 'Projet professionnel : schéma analytique', 'PROJET', 80, 'PROJECT'),
    ('SQL-10', 10, 'defi-final-optimiser-une-charge-reelle', 'Défi final : optimiser une charge réelle', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'SQL';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('DEVOPS-01', 1, 'images-docker-dockerfile-et-builds-reproductibles', 'Images Docker, Dockerfile et builds reproductibles', 'BASE', 70, 'LAB'),
    ('DEVOPS-02', 2, 'compose-reseaux-volumes-et-sante', 'Compose, réseaux, volumes et santé', 'BASE', 70, 'LAB'),
    ('DEVOPS-03', 3, 'securite-secrets-et-moindre-privilege', 'Sécurité, secrets et moindre privilège', 'INTERMEDIAIRE', 70, 'LAB'),
    ('DEVOPS-04', 4, 'pipeline-ci-cache-et-parallelisation', 'Pipeline CI, cache et parallélisation', 'INTERMEDIAIRE', 70, 'LAB'),
    ('DEVOPS-05', 5, 'quality-gates-artefacts-et-tracabilite', 'Quality gates, artefacts et traçabilité', 'INTERMEDIAIRE', 70, 'LAB'),
    ('DEVOPS-06', 6, 'observabilite-deploiement-et-rollback', 'Observabilité, déploiement et rollback', 'PROFESSIONNEL', 75, 'LAB'),
    ('DEVOPS-07', 7, 'projet-professionnel-chaine-de-livraison', 'Projet professionnel : chaîne de livraison', 'PROJET', 80, 'PROJECT'),
    ('DEVOPS-08', 8, 'incident-final-diagnostiquer-et-restaurer', 'Incident final : diagnostiquer et restaurer', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'DEVOPS';
