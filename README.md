# DLR — Deep Learning & Review

DLR est une application locale d'apprentissage de la programmation, avec évaluation déterministe, exécution isolée et professeur Ollama optionnel.

## État actuel

- API Spring Boot Java 21 ;
- application Angular standalone en thème sombre ;
- persistance PostgreSQL versionnée par Flyway ;
- 124 activités disponibles : Java 24, Python 24, TypeScript 24, Spring Boot 12, Angular 10, SQL 10, Docker/CI-CD 8 et Learn LLMs 12 ;
- parcours, laboratoire, éditeur Monaco, reprise de brouillon, quiz, checklist et import `.java` ;
- exécution Docker isolée, assertions privées et score déterministe ;
- tableau de bord, concepts clés, révisions espacées et planning ;
- planning glissant : seule la prochaine activité est datée, après la fin effective de son prérequis ;
- six thèmes professionnels persistants, avec palettes complètes et contraste contrôlé ;
- déploiement Render reproductible par Blueprint, API Java 21 conteneurisée et configuration Angular injectée au build ;
- profil local modifiable et professeur Ollama en mode explication, indice et correction qualitative ;
- PWA installable avec cache du shell et brouillons locaux IndexedDB ;
- documentation de conception et d'implémentation dans [`docs/`](docs/) ;
- journal de développement dans [`docs/DLR_CodeReview.md`](docs/DLR_CodeReview.md).
- guide de déploiement actuel et futur avec Neon et Render dans [`docs/DLR_Deploye.md`](docs/DLR_Deploye.md) ;
- synchronisation V2 par appareils appairés, journal idempotent et conflits sans perte.
- recommandations adaptatives expliquées, analytics d'autonomie et professeur Ollama à cinq rôles ;
- portfolio privé avec aperçu Markdown, export ZIP GitHub-ready et filtrage des secrets ;
- catalogue V2 extensible avec Java stable, Python/TypeScript/Learn LLMs en bêta et Spring Boot, Angular, SQL et DevOps planifiés.

## Structure

```text
apps/api/       API Spring Boot
apps/web/       application Angular
content/        contenu pédagogique versionné
infrastructure/ configuration locale
docs/           conception, implémentation et journal de développement
```

## Démarrage du backend

Prérequis : Java 21, Maven 3.8+ et Docker Desktop démarré.

Depuis la racine du dépôt, préparer PostgreSQL et l'image du runner :

```powershell
docker compose up -d postgres
.\infrastructure\scripts\build-runners.ps1
```

Le port PostgreSQL hôte par défaut est `5434` afin de ne pas entrer en conflit avec les autres instances locales.

```powershell
cd apps/api
mvn spring-boot:run
```

La page d'accueil de l'API est disponible sur `http://localhost:8081/` et les ressources métier sous `http://localhost:8081/api`.
Sa spécification OpenAPI est exposée sur `http://localhost:8081/api-docs` et l'interface Swagger sur `http://localhost:8081/swagger-ui.html`.

## Tests backend

```powershell
cd apps/api
mvn test
```

## Démarrage du frontend

Prérequis : Node.js et npm fonctionnel.

```powershell
cd apps/web
npm install
npm start
```

Le frontend utilise par défaut l'API `http://localhost:8081/api`.

La mise en ligne utilise [`render.yaml`](render.yaml) : Render crée `dlr-api` et `dlr-web`, puis demande les trois paramètres Neon sans les stocker dans Git. Le guide complet est disponible dans [`docs/DLR_Deploye.md`](docs/DLR_Deploye.md).

## Infrastructure locale

`compose.yaml` prépare PostgreSQL. Docker Desktop doit être démarré. Si la commande `docker` n'est pas dans le `PATH`, le script détecte automatiquement l'installation utilisateur de Docker Desktop.

Construire les images isolées des runners Java, Python et TypeScript :

```powershell
.\infrastructure\scripts\build-runners.ps1
```

Les runners désactivent le réseau, utilisent un utilisateur non privilégié, un système de fichiers en lecture seule, des limites CPU/mémoire/processus et un timeout côté backend.

## Parcours V2.5

Le catalogue expose une première tranche verticale exécutable pour `PYTHON-01`, `TYPESCRIPT-01` et `LLM-01`. Learn LLMs dépend du parcours Python et réutilise son runner pour les expériences reproductibles hors réseau. Les nombres d'activités affichés dans les cartes sont les cibles finales ; la mention **Bêta** distingue clairement les premières activités déjà disponibles.

Dans chaque laboratoire, un concept clé est associé à une section et apparaît avant le contenu concerné. L'éditeur Monaco adapte la coloration, l'import (`.java`, `.py`, `.ts`) et la requête d'exécution au langage du laboratoire.

## Parcours V2.6 — Python professionnel

Le parcours Python livre maintenant six étapes exécutables : `PYTHON-01` à `PYTHON-05`, puis le projet intermédiaire `PYTHON-06`. Les nouvelles activités couvrent fonctions et contrats, collections et compréhensions, fichiers JSON, dataclasses et pipeline de données.

La route `GET /api/paths/PYTHON/progress` calcule une progression dédiée et le prochain laboratoire. Chaque étape dépend de la précédente ; une tentative ne peut être créée que lorsque ses prérequis sont validés. Un résultat sous le seuil ne débloque la suite que si l'apprenant confirme explicitement qu'il continue. L'interface expose les états disponible, en cours, action requise, terminé et verrouillé, avec le prérequis manquant.

Chaque laboratoire propose aussi **Réinitialiser ce laboratoire**. Après confirmation, cette action efface uniquement ses tentatives, scores, exécutions, réponses, checklist, révisions et brouillon local. Le catalogue distingue désormais le contenu réellement disponible de la cible finale, par exemple `6 disponibles / 24 prévues` pour Python.

Le prérequis de parcours Java reste recommandé dans le catalogue. La séquence exécutable Python commence à `PYTHON-01` afin de préserver les données et usages déjà créés avec la V2.5.

## Parcours V2.7 — Java professionnel

Le parcours Java livre maintenant 24 activités séquentielles : 22 laboratoires, le projet portfolio `JAVA-23` et le défi final `JAVA-24`. Il couvre les fondations, l'objet, les collections, les génériques, les Streams, les fichiers, les tests, SOLID, les patterns, la concurrence, JDBC, Spring Boot, les API REST, la persistance, la sécurité, la performance, l'observabilité et Docker.

La route `GET /api/paths/JAVA/progress` expose les 24 états et le prérequis de chaque étape. Le catalogue affiche `24 disponibles / 24 prévues`, tandis que Python reste honnêtement indiqué à `6 / 24`. Les preuves Java sont également disponibles dans le portfolio et le tableau de bord débloque un badge professionnel après les 24 validations.

Les programmes Java s'exécutent dans `/work`, un espace temporaire inscriptible du conteneur isolé. Cela permet les exercices sur les fichiers tout en conservant le réseau coupé, la racine en lecture seule et les limites de ressources.

## Catalogue V3 — parcours professionnels étendus

Les parcours professionnels disposent maintenant de leur progression complète : Python et TypeScript sur 24 activités, Spring Boot et Learn LLMs sur 12, Angular et SQL sur 10, Docker/CI-CD sur 8. Chaque séquence termine par un projet portfolio puis un défi final.

La page **Mon parcours** charge les huit progressions mais n'affiche qu'une grille sélectionnée à la fois. Cette organisation maintient le DOM léger malgré les 124 contenus. Les cartes du catalogue servent de sélecteur et conservent les états disponible, en cours, verrouillé, projet et défi.

### V3.1 — Learn LLMs et projets framework

`LLM-01` à `LLM-12` couvrent prompts, échantillonnage, sorties structurées, évaluation, embeddings, chunking, recherche, RAG, sécurité, projet portfolio et défi final. Toutes les preuves restent reproductibles hors réseau dans le runner Python ; elles distinguent explicitement la simulation pédagogique d'un appel à un modèle réel.

Deux workspaces multi-fichiers sont livrés dans `projects/` : une API Spring Boot testée par MockMvc et un dashboard Angular standalone strict. Ils matérialisent les projets `SPRING_BOOT-11/12` et `ANGULAR-09/10` au-delà de la preuve unifichier rapide présentée dans le laboratoire.

Les preuves Spring Boot utilisent Java 21, les preuves Angular utilisent TypeScript strict, et les contrôles Docker/CI-CD utilisent Python. Les laboratoires SQL exécutent de vraies instructions SQL dans SQLite en mémoire afin de rester reproductibles et hors réseau ; les différences PostgreSQL sont explicitement indiquées dans les cours. Ces preuves unifichier valident les concepts et préparent les projets complets sans prétendre remplacer une application Spring ou Angular multi-fichiers.

## Sauvegarde locale

Créer une sauvegarde PostgreSQL binaire dans le dossier ignoré `backups/` :

```powershell
.\infrastructure\scripts\backup-database.ps1
```

Restaurer explicitement une sauvegarde (cette opération remplace les données actuelles) :

```powershell
.\infrastructure\scripts\restore-database.ps1 -BackupFile .\backups\dlr-AAAAMMJJ-HHMMSS.dump -Force
```

## Professeur local

Ollama est optionnel : DLR reste utilisable si le serveur est arrêté. Le modèle V1 par défaut est `llama3.1:latest` et se configure avec `DLR_OLLAMA_MODEL`. Les prompts bruts ne sont pas persistés ; seules leur empreinte et la réponse locale le sont.

## Synchronisation V2

Le PC principal peut appairer son navigateur sans code depuis l’écran Paramètres. Pour autoriser un autre appareil, définis un code privé et les origines frontend exactes avant de démarrer l’API :

```powershell
$env:DLR_SYNC_PAIRING_CODE="un-code-long-et-prive"
$env:DLR_ALLOWED_ORIGINS="http://localhost:4200,http://192.168.1.20:4200"
```

Chaque appareil reçoit un jeton affiché une seule fois et stocké uniquement dans son navigateur. Les envois sont idempotents ; deux modifications concurrentes d’une même version sont conservées comme variantes conflictuelles.

## Coach et portfolio V2

L'écran **Coach V2** explique la prochaine activité recommandée et laisse accepter, reporter, remplacer ou ignorer la proposition. Les indicateurs d'autonomie sont des estimations locales accompagnées de leurs facteurs. Le tuteur propose cinq rôles : professeur, coach, reviewer, client et Tech Lead. Aucun rôle recruteur n'est implémenté.

L'écran **Portfolio** crée des projets privés depuis des preuves sélectionnées. Il génère un README et une archive ZIP locale ; il ne lit ni scores, ni profil, ni conversations IA. Un motif ressemblant à un secret bloque l'opération avant persistance.

Les routes principales ajoutées sont :

- `GET /api/adaptation/recommendation` et `POST /api/adaptation/recommendations/{id}/decision` ;
- `GET /api/adaptation/insights` ;
- `POST /api/tutor/consult` ;
- `GET|POST /api/portfolio/projects`, aperçu README et export ZIP ;
- `GET /api/paths/catalog`.
