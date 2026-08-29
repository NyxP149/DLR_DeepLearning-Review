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
- journal de développement dans `DLR_CodeReview.md`.

## Structure

```text
apps/api/       API Spring Boot
apps/web/       application Angular
content/        contenu pédagogique versionné
infrastructure/ configuration locale
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
