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

## Déploiement cloud

- [x] Préparation du socle Render (`render.yaml`, Dockerfile Java 21, health check, CORS, `runtime-config.js`)
- [x] Mode hybride privé et gratuit : frontend Render + API/Docker/Ollama locaux via Tailscale Serve
- [ ] Déploiement réel Neon + Render validé (checklist de recette dans `docs/DLR_Deploye.md`)
- [ ] Service Runner distant isolé pour Java/Python/TypeScript (travaux signés, file d'attente, workers en bac à sable — Partie B de `docs/DLR_Deploye.md`)
- [ ] Professeur IA distant pour les déploiements cloud sans Ollama local
- [ ] Packaging et lancement simplifié sous Windows
