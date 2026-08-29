# DLR — Deep Learning & Review

DLR est une application locale d'apprentissage de la programmation. La V1 commence par une tranche verticale autour du laboratoire Java 1 avant d'étendre le parcours.

## État actuel

- monorepo initialisé ;
- API Spring Boot Java 21 ;
- squelette Angular standalone ;
- PostgreSQL et Flyway préparés ;
- six laboratoires Java fondamentaux versionnés ;
- catalogue et vue du parcours Java 1 à 6 ;
- exécution isolée, assertions de résultat et calcul déterministe du score ;
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

Ollama sera branché dans une étape suivante.
