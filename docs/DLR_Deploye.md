# DLR — Guide de déploiement Neon et Render

Dernière mise à jour : 2 septembre 2026.

Ce document décrit deux cibles distinctes :

1. le déploiement hébergé réalisable maintenant pour l'interface, l'API et les données ;
2. le déploiement futur complet, avec exécution isolée des laboratoires Java, Python et TypeScript.

Le dépôt source est : <https://github.com/NyxP149/DLR_DeepLearning-Review>.

## Résultat recherché

Après le premier déploiement, l'application ne dépend plus du démarrage manuel de trois terminaux sur le PC :

```text
Navigateur
   |
   | HTTPS
   v
Angular — Render Static Site
   |
   | HTTPS / JSON
   v
Spring Boot — Render Web Service
   |
   | JDBC + TLS
   v
PostgreSQL — Neon
```

Les données sont conservées par Neon et les nouveaux commits de la branche `main` peuvent être redéployés automatiquement par Render.

> Important : le dépôt n'est pas encore directement déployable tel quel. Le frontend contient encore des URL `localhost`, l'API ne lit pas encore directement la variable `PORT` de Render et aucun Dockerfile de production n'existe pour l'API. Ces adaptations doivent être livrées avant de suivre la partie Render.

## Ce que tu dois préparer

- un compte GitHub ayant accès au dépôt DLR ;
- un compte Neon ;
- un compte Render ;
- une adresse e-mail accessible pour les alertes de service ;
- un gestionnaire de mots de passe pour conserver les secrets de production.

Ne place jamais un mot de passe Neon, un jeton Render ou une clé d'API dans GitHub, une capture d'écran publique ou un fichier suivi par Git.

## Répartition des responsabilités

| Action | Où ? | Responsable |
|---|---|---|
| Créer le projet PostgreSQL | Console Neon | Toi |
| Copier les paramètres de connexion | Console Neon | Toi |
| Préparer la configuration de production | Dépôt GitHub | Code DLR |
| Créer le Web Service API | Console Render | Toi |
| Créer le Static Site Angular | Console Render | Toi |
| Saisir les secrets | Variables Render | Toi |
| Vérifier les migrations et les URL | Navigateur et journaux Render | Toi + contrôles DLR |
| Déployer les runners isolés | Infrastructure future | Étape ultérieure |

# Partie A — Déploiement hébergé actuel

## Étape 0 — Préparation du code avant les consoles

Le commit de préparation au déploiement devra fournir les éléments suivants :

- une seule configuration d'URL d'API pour Angular, locale en développement et Render en production ;
- la lecture de `PORT` par Spring Boot, avec `8081` comme valeur locale par défaut ;
- un Dockerfile de production Java 21 pour `apps/api` ;
- un fichier `render.yaml` ou des paramètres Render documentés et reproductibles ;
- un contrôle de santé sur `/actuator/health` ;
- la configuration CORS par `DLR_ALLOWED_ORIGINS` ;
- un mode explicite indiquant que l'exécution de code distante n'est pas encore disponible.

Ne lance pas le déploiement public avant que le build frontend, les tests backend et les tests end-to-end passent avec cette configuration.

## Étape 1 — Créer PostgreSQL sur Neon

1. Ouvre <https://console.neon.tech/> et connecte-toi.
2. Choisis **New project**.
3. Utilise le nom `dlr-production`.
4. Choisis PostgreSQL dans une région européenne proche de Render. Pour DLR en Italie, **AWS Europe (Frankfurt)** est un bon choix.
5. Garde la branche principale proposée par Neon, généralement `main`.
6. Garde la base créée par défaut ou nomme-la `dlr` si l'écran le permet.
7. Crée le projet.

Dans le tableau de bord du projet :

1. clique sur **Connect** ;
2. sélectionne la branche principale, la base et le rôle de production ;
3. sélectionne une connexion compatible Java/JDBC si l'interface la propose ;
4. conserve séparément le nom d'utilisateur, le mot de passe, le nom d'hôte et le nom de la base ;
5. vérifie que SSL est exigé.

DLR utilise actuellement trois variables séparées :

```text
DLR_DB_URL
DLR_DB_USER
DLR_DB_PASSWORD
```

La valeur attendue pour `DLR_DB_URL` est une URL JDBC, par exemple :

```text
jdbc:postgresql://ep-exemple.eu-central-1.aws.neon.tech/neondb?sslmode=require
```

Elle ne doit pas commencer par `postgresql://`. Le préfixe attendu par Spring Boot et le pilote PostgreSQL est `jdbc:postgresql://`.

Pour la première version, utilise la connexion directe Neon. DLR possède déjà son pool JDBC côté Spring Boot. Une connexion Neon poolée pourra être évaluée ensuite si le nombre de connexions augmente.

### Sécurité Neon

- génère un mot de passe long et unique ;
- ne mets jamais ce mot de passe dans `application.yml` ;
- ne copie jamais la chaîne complète dans `DLR_CodeReview.md` ;
- active l'authentification multifacteur du compte si elle est proposée ;
- conserve les alertes de consommation ;
- choisis Frankfurt également pour l'API Render afin de réduire la latence.

### Ce que Flyway fera

Au premier démarrage valide de l'API, Flyway créera automatiquement les tables et chargera les migrations du catalogue DLR. Une base Neon neuve est donc le choix le plus simple pour le premier déploiement.

Les tentatives et progressions présentes dans PostgreSQL local ne sont pas transférées automatiquement. Pour une première mise en ligne, démarre avec une base neuve. Une migration des données locales devra être faite séparément avec une sauvegarde contrôlée, après validation du schéma distant.

## Étape 2 — Créer l'API Spring Boot sur Render

Cette étape suppose que le Dockerfile de production a été ajouté au dépôt.

1. Ouvre <https://dashboard.render.com/>.
2. Choisis **New**, puis **Web Service**.
3. Connecte ton compte GitHub.
4. Sélectionne `NyxP149/DLR_DeepLearning-Review`.
5. Utilise les valeurs suivantes :

| Champ Render | Valeur recommandée |
|---|---|
| Name | `dlr-api` |
| Region | `Frankfurt` |
| Branch | `main` |
| Runtime / Language | `Docker` |
| Dockerfile Path | chemin du Dockerfile API ajouté au dépôt |
| Health Check Path | `/actuator/health` |
| Auto-Deploy | activé après succès de la CI |

Pour tester sans coût, le plan gratuit est possible. Pour éviter la mise en veille après quinze minutes d'inactivité, choisis un plan payant toujours actif.

### Variables d'environnement de l'API

Dans **Environment**, ajoute les variables suivantes :

| Nom | Valeur |
|---|---|
| `DLR_DB_URL` | URL JDBC Neon avec `sslmode=require` |
| `DLR_DB_USER` | rôle PostgreSQL Neon |
| `DLR_DB_PASSWORD` | mot de passe Neon, marqué secret |
| `DLR_ALLOWED_ORIGINS` | URL exacte du frontend Render, à compléter après sa création |
| `DLR_SYNC_PAIRING_CODE` | code long, aléatoire et privé |
| `DLR_RUNNER_TIMEOUT_SECONDS` | `10` |
| `DLR_OLLAMA_URL` | laisser absent tant qu'aucun service IA distant n'est prévu |

Si l'adaptation Spring Boot n'utilise pas directement `PORT`, ajoute temporairement :

```text
DLR_API_PORT=10000
```

La solution durable reste de faire lire `${PORT:8081}` par Spring Boot. Render attend qu'un Web Service écoute sur `0.0.0.0` et sur le port fourni par la plateforme.

Ne définis pas `DLR_DOCKER_CLI` sur ce premier service distant : le moteur actuel des laboratoires ne peut pas lancer de sous-conteneurs Docker de manière sûre dans le Web Service API.

### Premier démarrage de l'API

1. clique sur **Create Web Service** ;
2. ouvre l'onglet **Logs** ;
3. vérifie la connexion PostgreSQL ;
4. vérifie que toutes les migrations Flyway sont appliquées ;
5. attends l'état **Live** ;
6. copie l'URL publique, par exemple `https://dlr-api.onrender.com` ;
7. ouvre `https://dlr-api.onrender.com/actuator/health`.

La réponse attendue est :

```json
{"status":"UP"}
```

Teste ensuite :

```text
https://dlr-api.onrender.com/
https://dlr-api.onrender.com/api/paths/catalog
https://dlr-api.onrender.com/swagger-ui.html
```

Le catalogue doit répondre avant de créer le frontend public.

## Étape 3 — Créer le frontend Angular sur Render

Cette étape suppose que la configuration Angular de production contient l'URL publique de l'API ou utilise une configuration injectée au déploiement.

1. Dans Render, choisis **New**, puis **Static Site**.
2. Sélectionne le même dépôt GitHub.
3. Saisis les valeurs suivantes :

| Champ Render | Valeur |
|---|---|
| Name | `dlr-web` |
| Branch | `main` |
| Root Directory | `apps/web` |
| Build Command | `npm ci && npm run build` |
| Publish Directory | `dist/dlr-web/browser` |
| Auto-Deploy | activé après succès de la CI |

Le Static Site est distribué par le CDN de Render ; aucune région n'est à choisir.

Si le patch de production utilise une variable de build, ajoute dans **Environment** la variable documentée par ce patch, avec la valeur :

```text
https://dlr-api.onrender.com/api
```

N'invente pas un nom de variable dans Render : utilise exactement celui défini dans la configuration Angular livrée avec le patch de déploiement.

### Routage Angular

Dans les règles **Redirects/Rewrites** du Static Site, ajoute :

| Source | Destination | Action |
|---|---|---|
| `/*` | `/index.html` | `Rewrite` |

Cette règle permet d'ouvrir directement `/paths`, `/planning`, `/settings` ou `/labs/...` sans recevoir une erreur 404 de Render.

### Premier démarrage du frontend

1. déclenche le déploiement ;
2. vérifie que `npm ci` puis `npm run build` réussissent ;
3. copie l'URL publique, par exemple `https://dlr-web.onrender.com` ;
4. ouvre la page d'accueil et `/paths`.

## Étape 4 — Finaliser CORS

Retourne dans le Web Service `dlr-api`, puis définis :

```text
DLR_ALLOWED_ORIGINS=https://dlr-web.onrender.com
```

Si un domaine personnalisé est ajouté plus tard, liste uniquement les origines exactes autorisées, séparées par des virgules :

```text
DLR_ALLOWED_ORIGINS=https://dlr-web.onrender.com,https://app.exemple.com
```

N'utilise pas `*` avec des opérations authentifiées ou des jetons d'appairage.

Après modification, redéploie ou redémarre l'API puis recharge le frontend. Dans les outils développeur du navigateur, aucune requête ne doit échouer pour une erreur CORS.

## Étape 5 — Checklist de recette

Valide les points suivants dans cet ordre :

- [ ] `/actuator/health` répond `UP` ;
- [ ] le catalogue affiche les huit parcours ;
- [ ] les 124 activités sont présentes ;
- [ ] une modification du profil persiste après actualisation ;
- [ ] le planning charge ses données ;
- [ ] les six thèmes restent sélectionnés après actualisation ;
- [ ] une tentative et un brouillon peuvent être créés ;
- [ ] la réinitialisation d'un laboratoire fonctionne ;
- [ ] les routes Angular directes ne renvoient pas 404 ;
- [ ] aucun appel du navigateur ne vise `localhost:8081` ;
- [ ] aucun secret n'apparaît dans les sources téléchargées du navigateur ;
- [ ] les journaux Render ne contiennent pas le mot de passe Neon ;
- [ ] l'interface indique clairement que l'exécution distante est temporairement indisponible tant que les runners ne sont pas déployés.

## Étape 6 — Comprendre les mises en veille

### Render gratuit

Un Web Service gratuit s'arrête après quinze minutes sans requête entrante. La première ouverture suivante peut prendre environ une minute. L'URL reste valide, mais l'utilisateur voit un démarrage à froid.

Pour une API immédiatement disponible en permanence, utilise une instance Render payante. Le Static Site Angular, lui, reste servi par le CDN.

### Neon gratuit

Le calcul PostgreSQL Neon Free passe à zéro après environ cinq minutes d'inactivité. Il se réactive automatiquement à la prochaine connexion, généralement en quelques centaines de millisecondes. Cela ne supprime pas les données.

Le premier appel après une longue période peut donc cumuler le réveil de Render et celui de Neon. Pour une démonstration importante, ouvre l'application une à deux minutes avant.

## Étape 7 — Alertes, sauvegardes et retour arrière

- active les notifications d'échec de déploiement Render ;
- surveille l'utilisation et le stockage dans Neon ;
- conserve les migrations Flyway immuables après leur publication ;
- crée une nouvelle migration pour toute correction de schéma ;
- utilise **Rollback** dans Render si une version casse l'API ;
- ne supprime jamais le projet Neon pour revenir à une version précédente du code ;
- teste périodiquement une restauration avant de considérer les sauvegardes comme fiables ;
- pour les données importantes, choisis une offre avec une durée de restauration et des garanties adaptées.

## Dépannage rapide

### Le frontend affiche une erreur réseau

1. vérifie l'état du Web Service API ;
2. ouvre `/actuator/health` ;
3. vérifie que l'URL d'API compilée ne contient pas `localhost` ;
4. vérifie `DLR_ALLOWED_ORIGINS` ;
5. consulte la requête en erreur dans l'onglet Réseau du navigateur.

### L'API ne démarre pas

Cherche dans les logs, dans cet ordre :

1. erreur de port ou absence d'écoute sur `0.0.0.0` ;
2. URL JDBC sans préfixe `jdbc:` ;
3. SSL Neon absent ;
4. identifiant ou mot de passe incorrect ;
5. migration Flyway en erreur ;
6. mémoire insuffisante pendant le démarrage Java.

### Les pages Angular directes renvoient 404

Vérifie la règle Render `/*` vers `/index.html` avec l'action **Rewrite**.

### La première ouverture est lente

Sur les offres gratuites, attends le réveil du Web Service Render. Consulte les logs pour distinguer un démarrage à froid d'une véritable panne.

# Partie B — Déploiement futur complet

## Pourquoi les runners doivent être séparés

Le backend actuel lance `docker run` pour exécuter du code Java, Python et TypeScript avec :

- réseau désactivé ;
- système de fichiers racine en lecture seule ;
- utilisateur non privilégié ;
- limites CPU, mémoire et nombre de processus ;
- délai maximal d'exécution ;
- suppression du conteneur après le test.

Le fait de déployer l'API elle-même avec Docker sur Render ne lui fournit pas automatiquement un moteur Docker imbriqué. Exécuter directement le code soumis dans le processus Spring Boot détruirait la séparation de sécurité.

## Architecture cible

```text
Render Static Site
        |
        v
Render API Spring Boot --------------------> Neon PostgreSQL
        |
        | requête signée / identifiant de travail
        v
File d'exécution
        |
        v
Service Runner privé
        |
        +--> bac à sable Java 21
        +--> bac à sable Python 3.13
        +--> bac à sable TypeScript / Node 22
        |
        v
Résultat limité et signé --> API --> Navigateur
```

## Exigences du futur service Runner

Le Runner devra :

- être inaccessible directement depuis Internet ;
- accepter uniquement des travaux signés par l'API ;
- créer un bac à sable neuf pour chaque exécution ;
- couper le réseau sortant par défaut ;
- limiter CPU, mémoire, disque, processus et durée ;
- limiter la taille du code, de stdout et de stderr ;
- supprimer tous les fichiers temporaires après chaque travail ;
- ne recevoir aucun secret Neon ou Render ;
- ne jamais monter le système de fichiers ou le socket Docker de l'API ;
- journaliser un identifiant technique sans conserver inutilement le code source ;
- appliquer un quota et un contrôle de débit par utilisateur ;
- refuser les images ou langages non autorisés ;
- être testé contre les boucles infinies, fork bombs, gros fichiers et sorties infinies.

## Découpage futur conseillé

### Phase B1 — Contrat d'exécution

- extraire une interface `ExecutionGateway` indépendante de Docker ;
- définir les états `QUEUED`, `RUNNING`, `PASSED`, `FAILED`, `TIMEOUT` et `INFRA_ERROR` ;
- rendre les requêtes idempotentes ;
- signer chaque travail avec une durée de validité courte ;
- séparer une erreur pédagogique d'une panne d'infrastructure.

### Phase B2 — File de travaux

- ajouter une file privée entre l'API et les workers ;
- ne jamais conserver le mot de passe Neon dans les workers ;
- appliquer une expiration aux travaux ;
- prévoir reprise, annulation et nettoyage ;
- retourner immédiatement un identifiant de suivi à l'interface.

### Phase B3 — Workers isolés

- publier les images versionnées des runners dans un registre privé ;
- exécuter chaque travail dans une isolation adaptée au code non fiable ;
- désactiver les privilèges et capacités système inutiles ;
- déployer plusieurs workers seulement après mesure de la charge ;
- ajouter des tests de sécurité avant l'ouverture à plusieurs utilisateurs.

### Phase B4 — Interface asynchrone

- afficher « en attente », « exécution » et « résultat » ;
- permettre l'annulation lorsque le worker le supporte ;
- gérer proprement un worker temporairement indisponible ;
- ne jamais transformer une panne du runner en score nul pour l'apprenant.

### Phase B5 — Exploitation

- tableaux de bord de durée, échecs et saturation ;
- alertes sur la file et les timeouts ;
- rotation des secrets de signature ;
- mise à jour régulière des images Java, Python et Node ;
- sauvegarde et restauration Neon testées ;
- environnement de préproduction séparé de la production.

## Professeur IA futur

Ollama est actuellement prévu comme service local optionnel. Une API Render ne peut pas appeler `localhost:11434` sur le PC de l'utilisateur.

Deux options futures sont possibles :

1. déployer un service de modèle privé accessible par l'API ;
2. intégrer un fournisseur d'IA distant avec clé secrète côté backend uniquement.

Dans les deux cas, le frontend ne doit jamais recevoir la clé. DLR doit rester utilisable si le professeur IA est indisponible.

## Passage de « maintenant » à « futur »

La mise en ligne doit progresser sans masquer les limites :

1. déployer Neon, l'API et Angular ;
2. valider le catalogue, la progression et la persistance ;
3. afficher l'exécution distante comme indisponible, sans simulation trompeuse ;
4. construire et auditer le service Runner ;
5. activer les exécutions sur un environnement de préproduction ;
6. effectuer des tests end-to-end et de sécurité ;
7. activer progressivement Java, puis Python, TypeScript et Learn LLMs ;
8. surveiller les quotas et les erreurs avant l'ouverture à davantage d'utilisateurs.

# Checklist finale du propriétaire

## Avant le déploiement

- [ ] comptes Neon, Render et GitHub protégés ;
- [ ] patch de production fusionné dans `main` ;
- [ ] tests automatisés réussis ;
- [ ] aucun secret dans Git ;
- [ ] stratégie de disponibilité gratuite ou payante choisie.

## Neon

- [ ] projet `dlr-production` créé à Frankfurt ;
- [ ] paramètres JDBC conservés de manière sûre ;
- [ ] SSL obligatoire ;
- [ ] alertes de consommation activées ;
- [ ] migrations Flyway vérifiées.

## Render API

- [ ] Web Service `dlr-api` créé à Frankfurt ;
- [ ] variables secrètes saisies ;
- [ ] health check configuré ;
- [ ] `/actuator/health` répond `UP` ;
- [ ] aucun runner Docker annoncé comme fonctionnel avant son déploiement réel.

## Render frontend

- [ ] Static Site `dlr-web` créé ;
- [ ] répertoire publié `dist/dlr-web/browser` ;
- [ ] URL d'API de production correcte ;
- [ ] rewrite Angular configuré ;
- [ ] CORS limité à l'origine réelle du frontend.

## Après le déploiement

- [ ] recette fonctionnelle terminée ;
- [ ] journaux sans secret ;
- [ ] alertes testées ;
- [ ] procédure de retour arrière connue ;
- [ ] limites de l'offre gratuite communiquées aux utilisateurs.

# Références officielles

- Neon — connexion : <https://neon.com/docs/connect/connect-from-any-app>
- Neon — connexion poolée : <https://neon.com/docs/connect/connection-pooling>
- Neon — mise à zéro automatique : <https://neon.com/docs/introduction/scale-to-zero>
- Render — Web Services : <https://render.com/docs/web-services>
- Render — Docker : <https://render.com/docs/docker>
- Render — Static Sites : <https://render.com/docs/static-sites>
- Render — redirects et rewrites : <https://render.com/docs/redirects-rewrites>
- Render — régions : <https://render.com/docs/regions>
- Render — limites du plan gratuit : <https://render.com/docs/free>
- Render — variables d'environnement : <https://render.com/docs/environment-variables>

