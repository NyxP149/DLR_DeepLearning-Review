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
- Ajout de la persistance des tentatives, soumissions et résultats d'exécution.
- Ajout du runner Java Docker et de l'éditeur Angular exécutable.

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

#### Spring ne sélectionne pas les constructeurs de production

- **Symptôme :** le contexte de test échoue avec `No default constructor found` sur `ExecutionService`.
- **Cause :** les services possèdent un constructeur public de production et un constructeur secondaire destiné aux tests déterministes ; avec plusieurs constructeurs, Spring exige un choix explicite.
- **Résolution :** annotation `@Autowired` sur les constructeurs de production de `AttemptService`, `ExecutionService` et `DockerJavaRunner`.
- **Prévention :** les constructeurs secondaires restent non publics et les tests de contexte Spring couvrent désormais leur instanciation.

#### Les chemins du script PowerShell du runner sont corrompus

- **Symptôme :** Docker reçoit un contexte ressemblant à `scripts\\....\\runnersjava-runner` et ne trouve pas le Dockerfile.
- **Cause :** les antislashs du texte généré ont été interprétés comme des séquences d'échappement, notamment `\r` et `\b`.
- **Résolution :** utilisation de séparateurs `/` dans les segments transmis à `Join-Path`, correctement pris en charge sous Windows.
- **Prévention :** le script est exécuté réellement pendant la validation de chaque modification du runner.

#### Le marqueur de patch est injecté dans le Dockerfile

- **Symptôme :** Alpine reçoit `addgroup ... runner + && adduser ...` et refuse l'argument supplémentaire.
- **Cause :** le caractère de continuation de ligne du Dockerfile a échappé le saut de ligne lors de la génération et conservé le `+` de la ligne suivante.
- **Résolution :** regroupement de la création du groupe et de l'utilisateur sur une instruction `RUN` unique.
- **Prévention :** éviter les continuations de ligne dans les petits Dockerfiles générés.

#### `javac` est introuvable dans l'image JDK

- **Symptôme :** même le programme valide retourne `COMPILATION_ERROR` avec `sh: javac: not found`.
- **Cause :** le runner utilisait `sh -lc`. Le shell de connexion Alpine recharge son profil et remplace le `PATH` fourni par l'image Eclipse Temurin, qui contient le répertoire du JDK.
- **Résolution :** utilisation de `sh -c`, qui conserve l'environnement et le `PATH` de l'image.
- **Prévention :** test d'intégration obligatoire compilant et exécutant réellement un programme Java valide.

#### Le port PostgreSQL prévu pour DLR est déjà occupé

- **Symptôme :** le port `5432` est publié par `lfm_languegesforme-postgres-1` et le port `5433` par `bts-postgres`.
- **Cause :** plusieurs projets Docker locaux utilisent déjà PostgreSQL.
- **Résolution :** DLR utilise par défaut le port hôte `5434`, configurable avec `DLR_DB_PORT` et `DLR_DB_URL`.
- **Prévention :** vérification des ports actifs avant le premier `docker compose up` ; aucun conteneur existant n'est arrêté ou modifié.

#### Le port HTTP 8080 est déjà utilisé

- **Symptôme :** `mailmind-frontend-1` publie déjà `127.0.0.1:8080`.
- **Cause :** DLR partage la machine avec plusieurs applications Docker locales.
- **Résolution :** l'API DLR utilise par défaut `8081`, configurable avec `DLR_API_PORT`; les services Angular pointent sur ce port.
- **Prévention :** les ports de développement DLR sont centralisés dans la configuration et l'exemple d'environnement.

#### Docker n'est pas disponible dans le PATH

- **Symptôme :** la commande `docker` est inconnue.
- **Cause réelle :** Docker Desktop est installé pour l'utilisateur dans `%LOCALAPPDATA%\Programs\DockerDesktop`, mais son dossier `resources\bin` n'est pas dans le `PATH` du sandbox.
- **Résolution :** détection automatique de cette installation utilisateur par le backend et par le script de construction des runners. La variable `DLR_DOCKER_CLI` reste disponible pour tout autre emplacement.
- **Validation :** Docker Desktop 4.83.0, client/Engine 29.6.2 et Compose 5.3.1 répondent correctement.
- **Impact :** le runner Java Docker peut maintenant être implémenté et validé.

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

#### Le démarrage Spring Boot hors ligne manque des dépendances du plugin

- **Symptôme :** `mvn --offline spring-boot:run` échoue alors que la compilation et les tests fonctionnent déjà hors ligne.
- **Cause :** certaines dépendances propres au plugin `spring-boot-maven-plugin` ne sont téléchargées qu'au premier lancement de l'application.
- **Résolution :** premier lancement Maven avec accès réseau autorisé et dépôt local isolé, puis réutilisation du cache pour les lancements suivants.
- **Impact :** aucun changement applicatif ; les dépendances restent dans le cache Maven ignoré par Git.

#### PostgreSQL DLR s'arrête après un changement d'état de Docker Desktop

- **Symptôme :** la vérification finale montre le conteneur DLR arrêté avec le code `255`, alors qu'il était sain pendant le test de bout en bout.
- **Cause probable :** interruption du moteur Docker Desktop ; les journaux applicatifs et les données ne signalent pas de panne PostgreSQL.
- **Résolution :** redémarrage ciblé avec `docker compose up -d postgres`, sans agir sur les autres conteneurs de la machine.
- **Validation :** le même volume est remonté, le port `5434` est republié et le contrôle de santé PostgreSQL repasse à `healthy`.

### Validations

- Backend : compilation Java 21 réussie.
- Flyway : migration V1 validée et appliquée sur H2 en mode PostgreSQL.
- Tests backend : 5 tests réussis, 0 échec, 0 erreur.
- Sécurité du quiz : test de non-exposition de la réponse correcte réussi.
- Frontend : build Angular de production réussi.
- Bundle frontend initial : 242,00 kB bruts, dont 7,35 kB chargés paresseusement pour le laboratoire.

## 2026-08-29 — Tentatives et runner Java Docker

### Modifications

- Migration Flyway V2 : tables `submission` et `execution_result`.
- API de création et reprise d'une tentative.
- API de sauvegarde d'une soumission Java.
- API de compilation et d'exécution d'une soumission.
- Détection automatique du Docker CLI installé pour l'utilisateur.
- Image `dlr/java-runner:21` basée sur Eclipse Temurin 21 JDK Alpine.
- Isolation : aucun réseau, système racine en lecture seule, utilisateur 10001, capacités Linux supprimées, `no-new-privileges`, limites de mémoire, CPU et processus.
- Limites applicatives : code source de 64 Kio, sorties de 64 Kio et timeout configurable.
- Connexion de l'éditeur Angular aux API de tentative, soumission et exécution.

### Décisions

- Le runner reste synchrone pour la première tranche verticale ; une file locale viendra lorsque plusieurs exécutions concurrentes devront être prises en charge.
- L'exécution Docker est effectuée hors transaction afin de ne pas conserver une connexion PostgreSQL pendant la compilation.
- Les flux standard sont drainés même après leur limite d'affichage afin d'éviter un blocage du processus ; seul leur contenu conservé est tronqué.
- Les tests Docker sont activés explicitement avec `DLR_RUN_DOCKER_TESTS=true`.

### Validations

- Image `dlr/java-runner:21` construite avec succès.
- Programme Java valide compilé et exécuté dans le conteneur.
- Erreur de compilation détectée avec le statut `COMPILATION_ERROR`.
- Boucle infinie interrompue avec le statut `TIMEOUT`.
- Migrations Flyway V1 et V2 appliquées avec succès en test.
- Suite finale : 12 tests réussis, 0 échec, dont 3 tests Docker réels.
- Build Angular de production réussi après connexion au runner.
- Bundle Angular actuel : 246,96 kB initiaux et 11,44 kB chargés paresseusement pour le laboratoire.
- Image vérifiée avec l'utilisateur non privilégié `10001:10001` ; aucun conteneur `dlr-java-*` abandonné.
- PostgreSQL DLR démarré et sain sur le port hôte `5434`, sans modifier les conteneurs déjà présents sur les ports `5432`, `5433` et `8080`.
- Parcours HTTP réel validé de bout en bout : santé de l'API, chargement de `JAVA-01`, création d'une tentative, sauvegarde d'une soumission, compilation et exécution Docker avec statut `SUCCESS` et sortie `DLR Java Lab 1`.
- API temporaire de validation arrêtée proprement après le test ; PostgreSQL DLR reste disponible pour la suite du développement.

## 2026-08-29 — Clôture déterministe d'un laboratoire

### Modifications

- Migration Flyway V3 : réponses de quiz, checklist, détail du score, décision de poursuivre et révisions programmées.
- Correction déterministe des choix simples et des réponses de connexion par critères conservés uniquement côté backend.
- Calcul du bilan à partir du dernier résultat Docker, du quiz, de la pratique, des connexions et de l'auto-évaluation.
- Statuts `COMPLETED` et `COMPLETED_BELOW_THRESHOLD`, avec poursuite explicite sous le seuil.
- Création automatique d'une révision à J+1 lorsqu'un score est insuffisant.
- Interface Angular complète pour répondre au quiz, remplir la checklist, afficher le détail du score et confirmer la poursuite.

### Décisions

- La version du barème (`V1`) et tous les sous-scores sont enregistrés avec la tentative afin de rendre le résultat reproductible.
- Une tentative ne peut être terminée sans exécution, sans toutes les réponses au quiz ou sans checklist.
- La réponse libre utilise des critères déterministes minimaux dans cette tranche. Ollama enrichira ensuite le feedback qualitatif sans remplacer ce score reproductible.

### Bugs et incidents

#### L'option du dépôt Maven est mal transmise par PowerShell

- **Symptôme :** Maven interprète une partie de `-Dmaven.repo.local=...` comme un objectif de plugin lorsque le chemin contient des espaces.
- **Cause :** transmission ambiguë de l'argument au lanceur natif Windows.
- **Résolution :** résolution explicite de `mvn.cmd`, variable PowerShell dédiée au chemin et argument complet placé entre guillemets.
- **Prévention :** conserver cette forme pour tous les lancements Maven dans ce workspace contenant une esperluette et des espaces.

#### Le premier chemin de cache Maven sélectionné est vide

- **Symptôme :** le mode hors ligne ne retrouve pas le parent Spring Boot.
- **Cause :** les dépendances existantes se trouvent dans `apps/api/.m2/repository`, et non dans le cache `.m2` créé par erreur à la racine.
- **Résolution :** utilisation du cache backend réel ; le dossier racine vide reste ignoré par Git.

#### Angular signale une coalescence nulle inutile

- **Symptôme :** diagnostic `NG8102` sur la valeur de la réponse libre.
- **Cause :** le type indexé du signal exclut déjà `null` et `undefined`.
- **Résolution :** suppression de l'opérateur `??` inutile dans le template.

### Validations

- Flyway V3 appliquée avec succès sur H2 en mode PostgreSQL et sur PostgreSQL 17.11 réel.
- Suite backend finale : 14 tests réussis, 0 échec, 0 ignoré, dont 3 tests Docker réels.
- Deux nouveaux parcours HTTP testés : validation à 100 % et score sous le seuil avec révision puis poursuite explicite.
- Parcours réel API → PostgreSQL → Docker → quiz → checklist → bilan : statut `COMPLETED`, score `100.00`, sortie `DLR Java Lab 1`.
- Build Angular réussi ; l'espace laboratoire lazy-loaded atteint 20,28 kB bruts. Seul l'avertissement connu du cache optionnel `lmdb` subsiste.
- API temporaire arrêtée après validation ; PostgreSQL DLR reste démarré.
