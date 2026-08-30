# DLR — Deep Learning & Review

DLR est une application locale d'apprentissage de la programmation, avec évaluation déterministe, exécution isolée et professeur Ollama optionnel.

## État actuel

- API Spring Boot Java 21 ;
- application Angular standalone en thème sombre ;
- persistance PostgreSQL versionnée par Flyway ;
- six laboratoires Java fondamentaux versionnés ;
- parcours, laboratoire, éditeur Monaco, reprise de brouillon, quiz, checklist et import `.java` ;
- exécution Docker isolée, assertions privées et score déterministe ;
- tableau de bord, concepts clés, révisions espacées et planning ;
- profil local modifiable et professeur Ollama en mode explication, indice et correction qualitative ;
- PWA installable avec cache du shell et brouillons locaux IndexedDB ;
- documentation de conception et d'implémentation dans [`docs/`](docs/) ;
- journal de développement dans [`docs/DLR_CodeReview.md`](docs/DLR_CodeReview.md).
- synchronisation V2 par appareils appairés, journal idempotent et conflits sans perte.
- recommandations adaptatives expliquées, analytics d'autonomie et professeur Ollama à cinq rôles ;
- portfolio privé avec aperçu Markdown, export ZIP GitHub-ready et filtrage des secrets ;
- catalogue V2 extensible avec Java disponible et parcours Spring Boot, Angular, SQL et DevOps planifiés.

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

L'API est ensuite disponible sur `http://localhost:8081/api`.
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

## Infrastructure locale

`compose.yaml` prépare PostgreSQL. Docker Desktop doit être démarré. Si la commande `docker` n'est pas dans le `PATH`, le script détecte automatiquement l'installation utilisateur de Docker Desktop.

Construire l'image isolée du runner Java :

```powershell
.\infrastructure\scripts\build-runners.ps1
```

Le runner désactive le réseau, utilise un utilisateur non privilégié, un système de fichiers en lecture seule, des limites CPU/mémoire/processus et un timeout côté backend.

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
