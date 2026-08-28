# DLR — Deep Learning & Review

DLR est une application locale d'apprentissage de la programmation. La V1 commence par une tranche verticale autour du laboratoire Java 1 avant d'étendre le parcours.

## État actuel

- monorepo initialisé ;
- API Spring Boot Java 21 ;
- squelette Angular standalone ;
- PostgreSQL et Flyway préparés ;
- contenu versionné du laboratoire Java 1 ;
- endpoint de consultation du laboratoire ;
- calcul déterministe du score ;
- journal de développement dans `DLR_CodeReview.md`.

## Structure

```text
apps/api/       API Spring Boot
apps/web/       application Angular
content/        contenu pédagogique versionné
infrastructure/ configuration locale
```

## Démarrage du backend

Prérequis : Java 21 et Maven 3.8+.

```powershell
cd apps/api
mvn spring-boot:run
```

L'API est ensuite disponible sur `http://localhost:8080/api`.

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

Le frontend utilise par défaut l'API `http://localhost:8080/api`.

## Infrastructure locale

`compose.yaml` prépare PostgreSQL. Le runner Docker et Ollama seront branchés dans les étapes suivantes.

