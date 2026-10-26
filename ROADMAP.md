# Roadmap

## V1 — MVP local

- [x] Monolithe modulaire : API Spring Boot Java 21 + Angular standalone
- [x] Persistance PostgreSQL versionnée par Flyway
- [x] Première tranche verticale : laboratoire Java 1 (cours, concept clé, éditeur Monaco, exécution Docker, quiz, score)
- [x] Score déterministe et runner Docker isolé (réseau coupé, utilisateur non privilégié, limites de ressources)
- [x] Maîtrise des concepts et révision espacée
- [x] Tableau de bord, calendrier et planning
- [x] Gamification : XP, niveaux, séries, badges
- [x] Professeur Ollama local (explication, indice, correction)
- [x] PWA installable avec cache et brouillons IndexedDB
- [x] Parcours Java complet : 24 activités, projet portfolio et défi final

## V2 — Synchronisation, adaptation, portfolio

- [x] Synchronisation multi-appareils idempotente avec appairage et variantes de conflit
- [x] Moteur de recommandation adaptative (module `adaptation`)
- [x] Professeur IA multi-rôles (teacher, coach, reviewer, client, tech lead)
- [x] Portfolio privé avec export README/ZIP et filtrage des secrets
- [x] Parcours Python professionnel V2.6 (tranche exécutable 6/24)

## V3 — Extension des parcours professionnels

- [x] Parcours Python et TypeScript complets (24/24 chacun)
- [x] Parcours Spring Boot (12/12), Angular (10/10), SQL (10/10), Docker/CI-CD (8/8)
- [x] Parcours Learn LLMs complet (12/12) avec projet RAG et défi de comparaison
- [x] Projets portfolio multi-fichiers (API Spring Boot, dashboard Angular) et suite Playwright E2E
- [x] Planning glissant basé sur les dates de fin effective
- [x] Six thèmes d'interface accessibles (contraste WCAG AA)
- [x] Parcours Architecture Système complet (24/24)
- [x] Catalogue étendu à 148 activités sur neuf parcours
- [x] Carnet personnel par laboratoire, page de notes groupée par langage et mémoire des réponses de réflexion (V3.7)
- [x] Éditeur simple en alternative à Monaco, export du fichier vers un IDE externe et brouillon conservé au rechargement (V3.9)
- [x] Éditeur de code migré de Monaco vers CodeMirror 6, plus léger et fiable au clavier (V3.10)
- [x] Exemple professionnel et extrait de code ajoutés à chacun des 148 concepts clés, sur les neuf parcours (V3.11)
- [x] Fenêtre de réponse du tuteur Ollama redimensionnée pour s'adapter entièrement au texte, sans troncature (V3.11B)
- [x] Le bilan de fin de laboratoire (score et répartition) survit désormais à un rechargement de page (V3.12)
- [x] Modèle Ollama par défaut allégé (llama3.2, 3B) et message d'échec de test affichant la sortie attendue
- [x] Plafond de génération du tuteur Ollama relevé (220 → 500 tokens) pour réduire les réponses coupées en plein mot
- [x] Recalcul du score d'un laboratoire déjà validé sans réinitialisation complète (V3.14)
- [x] Notes personnelles : vue liste et cartes (grandes/petites) au choix, mémorisée entre les visites (V3.15)
- [x] API DeepSeek intégrée comme second fournisseur du professeur IA, sélection par configuration sans bean Spring ambigu (V3.16)
- [x] DeepSeek devient le fournisseur IA par défaut ; le lanceur hybride demande la clé à chaque session et bascule automatiquement sur Ollama si elle est absente (V3.16)
- [x] Révisions : le score affiché suit désormais les recalculs du laboratoire, et le rôle des boutons « Réussi » / « Encore difficile » est couvert par des tests (V3.17)
- [x] Révisions : « Réussi » refusé tant que le score du laboratoire est sous son seuil (bouton désactivé et message explicatif) (V3.18)
- [x] Révisions : score toujours relu en direct, doublons de révision nettoyés (migration V19) et boutons verrouillés pendant l'envoi (V3.19)
- [x] Coach adaptatif : une proposition acceptée n'affiche plus de boutons qui échouaient, « Autre proposition » et « Ignorer » restent possibles (V3.19)
- [x] Exercices guidés : 106 codes de départ convertis en squelettes à compléter (Java 7-24, Spring Boot, Python, TypeScript, Angular, SQL, DevOps) au lieu de donner la solution (V3.20)
- [x] Architecture (24) et LLM (12) convertis aussi en squelettes à compléter, énoncés réécrits : 142 exercices sur 148 ne donnent plus la solution (V3.21)
- [x] Exercices à compléter propres à chaque sujet rédigés pour les 81 laboratoires qui partageaient le gabarit « preuve » : Spring Boot (12), Angular (10), SQL (10), DevOps (8), Python 7 à 24 (18) et TypeScript 2 à 24 (23) (V3.22)
- [x] Parcours « Terminal et IA en ligne de commande » (24 activités, sans prérequis) : bash, PowerShell, Git, automatisation et pilotage d'un projet avec Claude Code, exécutés dans un nouveau runner `dlr/shell-runner:1` ; catalogue à 172 activités sur dix parcours (V3.23)
- [x] Portfolio : le brouillon du formulaire (preuves cochées, titre, résumé, décisions) est enregistré en base et restauré au chargement ; tableau de bord : panneau « Activité récente » repliable et mémorisé (V3.24)
- [x] Pratique de récupération : révisions par question de rappel sans le cours, confiance déclarée avant correction, feedback immédiat par question, indices comptés dans la note, score V2 pour les nouvelles tentatives (V3.25)
- [x] Laboratoire pilote JAVA-10 réécrit avec le nouveau modèle (cours détaillé, exemple travaillé, 5 questions expliquées, exercice vérifié dans Docker) (V3.25)
- [x] Modèle V3.25 déployé sur 138 laboratoires : Java 11-24, Spring Boot, Python, TypeScript, Angular, SQL, DevOps, LLM, Architecture (cours en sections, quiz expliqués, exercices validés dans le conteneur) (V3.26)
- [x] Modèle v2 appliqué aussi au parcours Terminal (24) et à JAVA-01 à 09 : les 172 laboratoires suivent le même format (V3.27)
- [x] Tableau de bord de calibration sur la page Coach : confiance déclarée contre réussite réelle, verdict, sources quiz et révisions, angles morts reliés aux labos (V3.28)
- [x] Quiz de départ avant le cours : trois questions sans correction ni effet sur le score, « Je ne sais pas » admis, comparées à la première réponse du quiz final dans le bilan (V3.29)
- [x] Mesure de rétention différée sur la page Coach : rappels des révisions rangés par délai depuis la fin du labo, comparés au quiz initial, labos retenus durablement et oublis à revoir (V3.30)
- [x] Exercices de transfert : plusieurs exercices par labo (guidé noté, transferts facultatifs sans indice ni score), soumissions par exercice, indicateur « Transfert confirmé » alimenté ; pilote JAVA-10 avec 2 transferts validés dans Docker (V3.31)
- [x] Exercices de transfert déployés aux 172 labos : un transfert par labo, chaque solution validée dans le runner Docker réel, tests de contenu étendus à tous les exercices (V3.32)
- [x] Évolution de la calibration dans le temps : écart hebdomadaire entre confiance déclarée et réussite réelle, tendance (amélioration, stable, dégradation) sur la page Coach (V3.33)
- [x] Gain du quiz de départ agrégé sur la page Coach : réussite avant le cours contre première réponse du quiz final sur tous les labos terminés, questions apprises ou perdues, détail par labo (V3.34)
- [x] Maîtrise des concepts pilotée par la rétention : « maîtrisé » exige un dernier rappel juste, confiant, à l'étape finale et 7 jours ou plus après la fin du labo ; un rappel raté renvoie le concept à revoir et passe en tête des recommandations (V3.35)

## Déploiement cloud

- [x] Préparation du socle Render (`render.yaml`, Dockerfile Java 21, health check, CORS, `runtime-config.js`)
- [x] Mode hybride privé et gratuit : frontend Render + API/Docker/Ollama locaux via Tailscale Serve
- [ ] Déploiement réel Neon + Render validé (checklist de recette dans `docs/DLR_Deploye.md`)
- [ ] Service Runner distant isolé pour Java/Python/TypeScript (travaux signés, file d'attente, workers en bac à sable — Partie B de `docs/DLR_Deploye.md`)
- [x] Professeur IA distant pour les déploiements cloud sans Ollama local (API DeepSeek, V3.16)
- [ ] Packaging et lancement simplifié sous Windows
