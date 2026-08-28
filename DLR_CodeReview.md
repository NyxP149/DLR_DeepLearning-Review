# DLR — Journal d'implémentation et revue de code

## Rôle du document

Ce fichier conserve l'historique des modifications, décisions techniques, bugs rencontrés, causes identifiées, corrections appliquées et validations effectuées. Il doit être mis à jour à chaque tranche de développement significative.

## 2026-08-29 — Initialisation du projet

### Modifications

- Initialisation du monorepo DLR.
- Ajout du backend Spring Boot basé sur Java 21.
- Ajout du squelette frontend Angular avec composants standalone.
- Ajout de la configuration PostgreSQL/Flyway et du fichier `compose.yaml`.
- Ajout du premier contenu pédagogique versionné : Java, laboratoire 1.
- Ajout de l'API de consultation des parcours et laboratoires.
- Ajout du calcul déterministe du score et de ses tests unitaires.

### Décisions

- Spring Boot 3.5.11 est utilisé car cette version est déjà présente dans le cache Maven local et reste compatible avec Java 21.
- Angular 21.2.22 est retenu : il prend en charge Node.js 24.11 installé sur la machine, contrairement à Angular 22 qui exige au minimum Node.js 24.15.
- Le contenu pédagogique initial utilise JSON afin de profiter directement de Jackson, déjà intégré à Spring Boot, et d'éviter une dépendance YAML supplémentaire dans la première tranche.
- Le premier laboratoire est chargé depuis les ressources versionnées. La persistance du catalogue en PostgreSQL sera ajoutée après validation de cette tranche verticale.
- Le score est calculé exclusivement côté backend.

### Bugs et incidents

#### Git refuse le dépôt local avec `dubious ownership`

- **Symptôme :** `git status` échoue en indiquant que le propriétaire du dossier diffère de l'utilisateur isolé exécutant Codex.
- **Cause :** le dossier appartient au compte Windows principal tandis que les commandes sont exécutées par un compte sandbox distinct.
- **Résolution :** utilisation de `git -c safe.directory=<chemin> ...` pour les inspections, sans modifier la configuration Git globale de l'utilisateur.
- **Impact :** aucun impact sur les fichiers du projet ; les commandes Git doivent conserver cette option dans cet environnement.

#### npm global est cassé dans l'environnement courant

- **Symptôme :** `npm --version` cherche `npm-cli.js` dans un dossier utilisateur inaccessible ou incomplet.
- **Cause :** le préfixe npm utilisateur prend le pas sur l'installation système de Node.js.
- **Résolution :** utilisation directe de `node.exe` et de `npm-cli.js` depuis l'installation système, avec un cache npm placé dans le projet.
- **Impact :** aucun sur le code ; 509 paquets ont été installés dans `node_modules`, dossier exclu de Git.

#### Le caractère `&` du chemin casse le script npm Angular

- **Symptôme :** `npm run build` interprète la partie `Review` du chemin du workspace comme une commande séparée et ne trouve plus `ng.js`.
- **Cause :** le shim Windows généré dans `node_modules/.bin` transmet mal un chemin contenant une esperluette à `cmd.exe`.
- **Résolution :** les scripts `start` et `build` appellent Angular CLI directement avec Node et un chemin relatif, sans passer par le shim Windows de `node_modules/.bin`.
- **Impact :** `npm run build` devient indépendant de l'esperluette présente dans le chemin du dépôt.

#### La réponse correcte du quiz était exposée par l'API

- **Symptôme :** `GET /api/labs/JAVA-01` sérialisait directement le modèle métier, y compris le champ `correctChoice`.
- **Cause :** absence initiale de DTO de sortie séparant le contenu affichable des données de correction.
- **Résolution :** ajout de `LabDetailResponse` et `QuizQuestionResponse` ; la solution reste dans le catalogue backend et n'est plus envoyée au navigateur.
- **Prévention :** ajout d'une assertion MockMvc vérifiant l'absence de `correctChoice` dans la réponse JSON.
- **Impact :** rupture volontaire du modèle frontend initial, corrigé en retirant ce champ de l'interface TypeScript publique.

#### Angular ne peut pas lire certains chemins parents dans le sandbox

- **Symptôme :** le compilateur signale `Cannot read directory ../../../../..` puis prétend ne pas résoudre des fichiers sources présents.
- **Cause :** le résolveur de modules remonte les répertoires parents et rencontre les restrictions de lecture du sandbox.
- **Résolution :** compilation Angular autorisée hors de cette restriction, limitée à la commande Angular CLI du projet.
- **Impact :** build réussi ; aucun changement de code n'était nécessaire.

#### Cache JavaScript Angular `lmdb` indisponible

- **Symptôme :** avertissement `Cannot find module 'lmdb'` pendant le build.
- **Cause :** le stockage de cache natif optionnel n'est pas disponible dans cet environnement.
- **Résolution :** aucune dépendance forcée ; Angular précise que cela n'affecte pas le contenu du build.
- **Impact :** compilation potentiellement plus lente uniquement.

#### Javac ne peut pas fermer certains JAR dans le sandbox

- **Symptôme :** après une première exécution réussie, Maven échoue de façon variable sur un JAR avec `java.nio.file.AccessDeniedException`.
- **Cause :** le fournisseur ZIP de Java résout le chemin réel des archives au moment de leur fermeture et rencontre une restriction du sandbox ; les empreintes et tailles des fichiers restent lisibles, ce qui écarte une corruption du cache.
- **Résolution :** exécution finale de Maven hors de cette restriction, en mode hors ligne et limitée au cache local déjà téléchargé.
- **Impact :** aucun changement applicatif ; les 5 tests repassent correctement.

#### Docker n'est pas disponible dans le PATH

- **Symptôme :** la commande `docker` est inconnue.
- **Cause :** Docker Desktop n'est pas installé ou n'est pas exposé à l'environnement courant.
- **Résolution :** la configuration Compose est créée, mais les runners de code restent désactivés tant que Docker n'est pas disponible.
- **Impact :** l'isolation et l'exécution du code utilisateur ne font pas partie de cette première tranche validable.

#### Maven cible un dépôt local non accessible

- **Symptôme :** le premier `mvn test` échoue avec `Could not create local repository at C:\\.m2\\repository`.
- **Cause :** le compte sandbox résout son dossier utilisateur vers `C:\\`, où il ne possède pas de droit d'écriture.
- **Résolution :** exécuter Maven avec un chemin absolu vers un dépôt local placé dans le workspace. La forme relative passée au lanceur Windows était ignorée ; le chemin absolu est correctement interprété.
- **Impact :** les dépendances absentes ont été récupérées dans ce dépôt isolé, qui est exclu de Git.

#### Accès réseau Maven bloqué dans le sandbox

- **Symptôme :** la résolution du parent Spring Boot échoue avec `Permission denied: getsockopt`.
- **Cause :** l'accès au dépôt Maven Central est désactivé dans le sandbox standard.
- **Résolution :** exécution approuvée de Maven avec accès réseau, limitée au téléchargement des dépendances dans le cache du projet.
- **Impact :** aucun artefact tiers n'est ajouté au dépôt Git ; seuls les caches locaux ignorés sont créés.

### Validations

- Backend : compilation Java 21 réussie.
- Flyway : migration V1 validée et appliquée sur H2 en mode PostgreSQL.
- Tests backend : 5 tests réussis, 0 échec, 0 erreur.
- Sécurité du quiz : test de non-exposition de la réponse correcte réussi.
- Frontend : build Angular de production réussi.
- Bundle frontend initial : 242,00 kB bruts, dont 7,35 kB chargés paresseusement pour le laboratoire.
