# DLR — Deep Learning & Review

## Document de conception — Version 1.0

**Statut :** conception initiale  
**Public de la V1 :** utilisateur unique  
**Plateforme :** application web responsive  
**Durée cible :** 3 à 4 mois  
**Parcours :** Java, Python et TypeScript

---

## 1. Vision du produit

DLR est une application personnelle d'apprentissage et de suivi destinée à consolider les bases de la programmation, conduire progressivement vers un niveau professionnel et mesurer les progrès de façon concrète.

L'apprentissage commence par Java, déjà mieux maîtrisé par l'utilisateur. Python et TypeScript sont ensuite enseignés par comparaison avec Java : syntaxe équivalente, différences de typage, paradigmes, erreurs fréquentes et cas d'utilisation professionnels.

L'application associe un programme fixe conçu à l'avance à une assistance locale fournie par Ollama. Le programme garantit une progression pédagogique cohérente ; l'IA enrichit les explications et adapte les révisions, sans remplacer les objectifs officiels des laboratoires.

## 2. Objectifs

### 2.1 Objectifs pédagogiques

- Renforcer les fondamentaux de Java puis atteindre un niveau professionnel avancé.
- Apprendre Python jusqu'à un niveau professionnel, en s'appuyant sur les acquis Java.
- Apprendre TypeScript jusqu'à un niveau professionnel et l'utiliser dans Angular.
- Relier la théorie à la pratique grâce à des laboratoires progressifs.
- Développer la capacité à expliquer, comparer, déboguer, tester et concevoir du code.
- Produire trois projets professionnels exploitables dans un portfolio.
- Identifier les faiblesses réelles et les transformer en défis personnalisés.

### 2.2 Objectifs produit

- Centraliser cours, exercices, quiz, code, corrections et statistiques.
- permettre le travail dans l'éditeur intégré ou dans un IDE externe ;
- tester automatiquement les solutions ;
- fournir des explications personnalisées avec Ollama installé localement ;
- suivre la régularité, les scores, le temps et la maîtrise des concepts ;
- proposer un planning réaliste sur 3 ou 4 mois.

## 3. Périmètre pédagogique

Chaque langage comporte 24 activités :

1. **Laboratoires 1 à 22 :** théorie, pratique, tests et révision progressive.
2. **Laboratoire 23 :** projet professionnel choisi par l'IA selon le parcours et les résultats.
3. **Laboratoire 24 :** défi final personnalisé ciblant les faiblesses observées.

Le produit contient donc **72 activités principales** : 66 laboratoires, 3 projets et 3 défis.

### 3.1 Structure obligatoire d'un laboratoire

Chaque laboratoire comprend :

1. objectifs mesurables ;
2. prérequis ;
3. cours synthétique ;
4. exemples commentés ;
5. comparaison avec Java pour Python et TypeScript ;
6. questions théoriques ;
7. questions reliant plusieurs concepts ;
8. exercice guidé ;
9. exercice autonome ;
10. tests automatiques ;
11. checklist d'auto-évaluation ;
12. correction et explications par l'IA ;
13. synthèse des erreurs ;
14. ressources et révision recommandée.

### 3.2 Bloc « Concept clé »

Lorsqu'un laboratoire introduit ou mobilise une notion indispensable, celle-ci doit être signalée par la mention visible **« Concept clé »**. Ce bloc ne doit pas seulement donner une définition : il doit permettre de comprendre la raison d'être du concept et de vérifier sa maîtrise.

Chaque bloc contient, lorsque les éléments sont pertinents :

1. **Concept clé :** nom précis de la notion ;
2. **Définition simple :** formulation accessible en une ou deux phrases ;
3. **Pourquoi il existe :** problème concret que le concept permet de résoudre ;
4. **Pourquoi il est important :** conséquences dans un projet professionnel ;
5. **Fonctionnement :** mécanisme expliqué étape par étape ;
6. **Exemple minimal :** exemple isolant uniquement la notion ;
7. **Exemple professionnel :** utilisation dans une situation réaliste ;
8. **Connexion :** relation avec les concepts déjà appris ;
9. **Comparaison Java/Python/TypeScript :** lorsque le concept possède un équivalent ou une différence notable ;
10. **Erreur fréquente :** confusion ou mauvaise pratique à éviter ;
11. **Question de maîtrise :** question à laquelle l'utilisateur doit répondre avec ses propres mots ;
12. **Preuve de maîtrise :** action ou exercice permettant de démontrer la compréhension.

Un concept est marqué comme clé lorsqu'il remplit au moins un des critères suivants : il sert de prérequis à plusieurs laboratoires, il apparaît souvent en entretien, il influence l'architecture ou la qualité du code, ou sa mauvaise compréhension provoque régulièrement des erreurs difficiles à diagnostiquer.

L'application doit permettre de retrouver tous les concepts clés dans une vue dédiée et afficher leur niveau de maîtrise, leurs connexions et leur prochaine date de révision.

## 4. Parcours Java

| Nº | Thème principal | Niveau indicatif |
|---:|---|---|
| 1 | Syntaxe, compilation, JVM et premier programme | Base |
| 2 | Variables, types primitifs, opérateurs et conversions | Base |
| 3 | Conditions, boucles et raisonnement algorithmique | Base |
| 4 | Méthodes, paramètres, retour, portée et surcharge | Base |
| 5 | Tableaux, chaînes et manipulation de données | Base |
| 6 | Classes, objets, constructeurs et encapsulation | Base |
| 7 | Héritage, composition et polymorphisme | Intermédiaire |
| 8 | Interfaces, classes abstraites et contrats | Intermédiaire |
| 9 | Exceptions, validation et gestion des erreurs | Intermédiaire |
| 10 | Collections, égalité, tri et comparateurs | Intermédiaire |
| 11 | Génériques et programmation réutilisable | Intermédiaire |
| 12 | Lambdas, interfaces fonctionnelles et Streams | Intermédiaire |
| 13 | Fichiers, sérialisation, dates et API standard | Intermédiaire |
| 14 | Immutabilité, records, Optional et bonnes pratiques | Avancé |
| 15 | Tests avec JUnit, Mockito et stratégie de test | Avancé |
| 16 | SOLID, clean code et refactoring | Avancé |
| 17 | Design patterns : Strategy, Factory, Builder et Template | Avancé |
| 18 | Concurrence, threads, exécuteurs et synchronisation | Avancé |
| 19 | JDBC, SQL, transactions et accès aux données | Professionnel |
| 20 | Spring Boot, injection de dépendances et architecture en couches | Professionnel |
| 21 | API REST, DTO, validation, JPA/Hibernate et sécurité de base | Professionnel |
| 22 | Performance, observabilité, Docker et préparation entretien | Professionnel |
| 23 | Projet professionnel Java personnalisé | Projet |
| 24 | Défi Java personnalisé selon les faiblesses | Défi |

## 5. Parcours Python

Les comparaisons portent notamment sur le typage dynamique face au typage statique de Java, l'indentation, les collections, les fonctions comme objets, les exceptions, les environnements virtuels et les idiomes Python.

| Nº | Thème principal | Niveau indicatif |
|---:|---|---|
| 1 | Syntaxe, interpréteur, environnement et comparaison avec la JVM | Base |
| 2 | Variables, types, conversions et typage dynamique | Base |
| 3 | Conditions, boucles et compréhension du flux | Base |
| 4 | Fonctions, paramètres, portée et annotations de types | Base |
| 5 | Chaînes, listes, tuples, ensembles et dictionnaires | Base |
| 6 | Modules, packages, imports et environnements virtuels | Base |
| 7 | Classes, objets, propriétés et dataclasses | Intermédiaire |
| 8 | Héritage, composition, protocoles et duck typing | Intermédiaire |
| 9 | Exceptions, validation et context managers | Intermédiaire |
| 10 | Compréhensions, unpacking et expressions idiomatiques | Intermédiaire |
| 11 | Itérateurs, générateurs et évaluation paresseuse | Intermédiaire |
| 12 | Lambdas, fonctions d'ordre supérieur et décorateurs | Intermédiaire |
| 13 | Fichiers, JSON, CSV, dates et chemins | Intermédiaire |
| 14 | Type hints, mypy, qualité et organisation du code | Avancé |
| 15 | Tests avec pytest, mocks et couverture | Avancé |
| 16 | Principes SOLID et design patterns adaptés à Python | Avancé |
| 17 | Logging, configuration et gestion des erreurs applicatives | Avancé |
| 18 | Concurrence, multiprocessing et programmation asynchrone | Avancé |
| 19 | SQL, ORM et transactions | Professionnel |
| 20 | Création d'API avec FastAPI | Professionnel |
| 21 | Validation, authentification, architecture et documentation API | Professionnel |
| 22 | Packaging, Docker, performance et préparation entretien | Professionnel |
| 23 | Projet professionnel Python personnalisé | Projet |
| 24 | Défi Python personnalisé selon les faiblesses | Défi |

## 6. Parcours TypeScript

Les comparaisons portent notamment sur le système de types Java/TypeScript, la compilation, les objets structurels, les unions, les génériques, le modèle asynchrone et l'écosystème navigateur/Node.js.

| Nº | Thème principal | Niveau indicatif |
|---:|---|---|
| 1 | JavaScript, TypeScript, compilation et environnement | Base |
| 2 | Variables, types primitifs, inférence et mode strict | Base |
| 3 | Conditions, boucles, fonctions et portée | Base |
| 4 | Tableaux, tuples, objets et destructuration | Base |
| 5 | Interfaces, alias, unions, intersections et littéraux | Base |
| 6 | Fonctions typées, paramètres optionnels et surcharge | Base |
| 7 | Classes, encapsulation, héritage et composition | Intermédiaire |
| 8 | Génériques, contraintes et utilitaires de types | Intermédiaire |
| 9 | Narrowing, gardes de types et discriminated unions | Intermédiaire |
| 10 | Modules, packages, configuration et qualité | Intermédiaire |
| 11 | Erreurs, exceptions et validation des données | Intermédiaire |
| 12 | Fonctions avancées et programmation fonctionnelle | Intermédiaire |
| 13 | Promises, async/await et modèle événementiel | Intermédiaire |
| 14 | Requêtes HTTP, JSON et contrats d'API | Avancé |
| 15 | Tests unitaires, mocks et stratégie de test | Avancé |
| 16 | SOLID, clean code et design patterns | Avancé |
| 17 | Types avancés : mapped, conditional et template literal types | Avancé |
| 18 | Sécurité du typage, linting et refactoring | Avancé |
| 19 | Angular : composants, templates et injection de dépendances | Professionnel |
| 20 | Angular : services, RxJS, formulaires et routing | Professionnel |
| 21 | État, architecture frontend et intégration API | Professionnel |
| 22 | Performance, build, Docker et préparation entretien | Professionnel |
| 23 | Projet professionnel TypeScript/Angular personnalisé | Projet |
| 24 | Défi TypeScript personnalisé selon les faiblesses | Défi |

## 7. Méthode d'évaluation

### 7.1 Composantes du score

| Composante | Pondération initiale |
|---|---:|
| Tests automatiques | 40 % |
| Quiz théorique | 20 % |
| Exercice pratique et qualité du code | 20 % |
| Connexion entre les concepts | 10 % |
| Auto-évaluation et explication personnelle | 10 % |

Les pondérations pourront varier selon la nature du laboratoire. Un laboratoire algorithmique privilégiera les tests ; un laboratoire d'architecture accordera davantage de poids à la justification des choix.

### 7.2 Seuil adaptatif recommandé

- Laboratoires 1 à 6 : 70 %.
- Laboratoires 7 à 14 : 75 %.
- Laboratoires 15 à 22 : 80 %.
- Projet 23 : 80 % et aucun critère critique manquant.
- Défi 24 : 85 %.

Le seuil est une recommandation, pas un verrou. L'utilisateur peut continuer, mais l'application marque les notions fragiles et programme leur révision.

### 7.3 Niveaux de maîtrise d'un concept

- **Non vu** : concept non commencé.
- **Découverte** : cours consulté, aucune validation.
- **En cours** : exercices partiellement réussis.
- **Acquis** : seuil du laboratoire atteint.
- **Solide** : réussite confirmée lors d'une révision espacée.
- **Maîtrisé** : concept réutilisé correctement dans un projet ou un défi.

## 8. IA locale avec Ollama

### 8.1 Responsabilités de l'IA

- expliquer une notion à différents niveaux de détail ;
- comparer Python ou TypeScript à l'équivalent Java ;
- poser des questions reliant plusieurs concepts ;
- analyser une réponse libre et signaler les imprécisions ;
- commenter la qualité du code après les tests déterministes ;
- générer des indices progressifs sans révéler immédiatement la solution ;
- créer des exercices supplémentaires ciblés ;
- sélectionner le projet professionnel du laboratoire 23 ;
- composer le défi 24 à partir des faiblesses mesurées ;
- préparer des simulations de questions d'entretien.

### 8.2 Garde-fous

- Les tests automatiques restent la source principale pour valider le comportement du code.
- L'IA ne modifie pas silencieusement le score déterministe.
- Toute correction IA indique les critères utilisés.
- L'utilisateur peut demander une seconde explication ou contester une correction.
- Les prompts et réponses utiles sont conservés localement avec possibilité de suppression.
- Si Ollama est indisponible, les cours, quiz et tests restent utilisables.

## 9. Planning d'apprentissage

### 9.1 Parcours recommandé sur 4 mois

Durée cible : **16 semaines**, environ **140 à 165 heures**.

- Du lundi au vendredi : 1 h 30 par jour.
- Samedi : 1 h consacrée à un laboratoire ou à un exercice pratique.
- Dimanche : 45 minutes de révision légère, statistiques et planification.
- Total indicatif : 9 h 15 par semaine.

Répartition initiale :

- semaines 1 à 6 : Java ;
- semaines 7 à 11 : Python ;
- semaines 12 à 16 : TypeScript ;
- les révisions Java continuent pendant les parcours suivants grâce à la répétition espacée.

### 9.2 Parcours accéléré sur 3 mois

Durée cible : **12 semaines**, environ **145 à 170 heures**.

- Du lundi au vendredi : 2 heures par jour.
- Samedi : 1 h 30.
- Dimanche : 1 heure de révision légère.
- Total indicatif : 12 h 30 par semaine.

Cette cadence sera proposée seulement si les statistiques montrent qu'elle reste soutenable.

### 9.3 Format d'une séance

Pour une séance de 90 minutes :

1. 10 minutes de rappel actif sans consulter le cours ;
2. 25 minutes de théorie et exemples ;
3. 5 minutes de pause ;
4. 30 minutes de pratique concentrée ;
5. 5 minutes de pause ;
6. 10 minutes de quiz et explication personnelle ;
7. 5 minutes de récompense et bilan.

Pour une séance de deux heures, deux blocs de 50 minutes sont séparés par une pause de 10 à 15 minutes.

### 9.4 « Salaire après l'effort »

L'application invite l'utilisateur à choisir une petite récompense seulement après l'objectif de séance : musique, vidéo courte, café, collation, promenade ou activité agréable. La récompense doit rester courte et ne pas casser la séance suivante. Les récompenses importantes sont liées aux jalons 6, 14, 22, 23 et 24.

## 10. Fonctionnalités de la V1

### 10.1 Tableau de bord

- progression globale et par langage ;
- activité du jour et prochaine révision ;
- temps prévu et temps réellement passé ;
- score moyen et tendance ;
- concepts forts et fragiles ;
- série de jours consécutifs ;
- points d'expérience, niveau, badges et réussites ;
- calendrier et historique des séances.

### 10.2 Espace laboratoire

- consignes, cours et exemples ;
- éditeur de code intégré ;
- import ou collage d'une solution écrite dans un IDE ;
- exécution des tests ;
- terminal et sortie lisible ;
- quiz et réponses libres ;
- indices progressifs ;
- correction Ollama ;
- checklist personnelle ;
- notes et favoris ;
- bouton de validation et décision de poursuivre malgré un seuil non atteint.

### 10.3 Révisions

- répétition espacée à J+1, J+3, J+7, J+14 et J+30 ;
- cartes de rappel issues des erreurs ;
- mini-exercices interlangages ;
- nouvelles questions formulées par Ollama ;
- priorité calculée selon score, récence et importance du concept.

### 10.4 Projets et défis

Avant le laboratoire 23, l'IA analyse les compétences validées, les intérêts professionnels et les faiblesses. Elle propose un projet avec cahier des charges, jalons, critères d'acceptation et tests. Le projet doit mobiliser l'ensemble des acquis importants du parcours.

Le défi 24 est généré après le projet. Il cible plusieurs faiblesses combinées, limite progressivement l'aide de l'IA et exige une explication des décisions techniques.

## 11. Architecture technique proposée

| Couche | Technologie | Rôle |
|---|---|---|
| Interface | Angular + TypeScript | application responsive, éditeur, quiz, tableaux de bord |
| API principale | Java + Spring Boot | règles métier, progression, scores, planning et orchestration |
| Persistance | PostgreSQL | utilisateurs, parcours, résultats, concepts et historique |
| Migration DB | Flyway | versionnement du schéma |
| IA locale | Ollama | explications, corrections qualitatives et personnalisation |
| Exécution du code | Workers isolés par langage | compilation/exécution et tests Java, Python et TypeScript |
| Isolation | Docker local | limites de temps, mémoire, réseau et fichiers |
| Temps réel | WebSocket ou SSE | retour progressif des tests et de l'IA |

Cette stack transforme la construction de DLR en projet d'apprentissage : le backend renforce Java/Spring, tandis que le frontend développe TypeScript/Angular.

## 12. Modules fonctionnels

1. **Catalogue pédagogique** : langages, laboratoires, concepts et prérequis.
2. **Moteur de progression** : scores, statuts, seuils et recommandations.
3. **Moteur d'évaluation** : tests, quiz, réponses libres et checklists.
4. **Exécution sécurisée** : compilation et tests dans des conteneurs limités.
5. **Tuteur Ollama** : conversations contextualisées et corrections.
6. **Planification** : calendrier, objectifs quotidiens et rattrapage.
7. **Révision espacée** : calcul et génération des rappels.
8. **Gamification** : XP, niveaux, séries, badges et récompenses.
9. **Statistiques** : temps, maîtrise, erreurs fréquentes et évolution.
10. **Projets adaptatifs** : sélection du projet et génération du défi final.

## 13. Modèle de données conceptuel

Entités principales :

- `UserProfile` : préférences, disponibilité et objectif professionnel ;
- `LearningPath` : Java, Python ou TypeScript ;
- `Lab` : contenu, niveau, seuil, prérequis et critères ;
- `Concept` : notion pédagogique et relations avec d'autres concepts ;
- `KeyConceptContent` : pourquoi, fonctionnement, exemples, erreurs et preuve de maîtrise ;
- `LabConcept` : importance d'un concept dans un laboratoire ;
- `Attempt` : tentative, durée, code et résultat global ;
- `TestResult` : tests réussis, échoués et messages ;
- `QuizAnswer` : réponse, correction et explication ;
- `ConceptMastery` : niveau de maîtrise et prochaine révision ;
- `StudySession` : durée planifiée, durée réelle, pauses et bilan ;
- `AiInteraction` : contexte, réponse et évaluation utilisateur ;
- `Achievement` : badge, XP et condition d'obtention ;
- `ProjectBrief` : projet proposé, critères et jalons ;
- `ChallengeProfile` : faiblesses ciblées et contraintes du défi.

## 14. Règles métier essentielles

- Java doit être commencé avant Python ; Python doit être commencé avant TypeScript.
- Le passage au laboratoire suivant reste possible sous le seuil recommandé.
- Un passage sous le seuil crée automatiquement des révisions prioritaires.
- Un concept n'est « solide » qu'après une validation différée.
- Tout concept marqué comme clé doit contenir au minimum le « pourquoi », une connexion et une preuve de maîtrise.
- Les tests automatiques et le quiz sont versionnés avec le laboratoire.
- Une nouvelle tentative ne supprime jamais l'historique des précédentes.
- L'XP récompense l'effort et la régularité, mais ne remplace pas la maîtrise.
- Une série peut tolérer un jour de repos planifié afin de ne pas encourager le surmenage.
- Le projet et le défi doivent enregistrer la justification de leur sélection.
- Les appels à Ollama doivent pouvoir être désactivés.

## 15. Exigences non fonctionnelles

- Interface adaptée au PC, à la tablette et au mobile ; l'écriture de code reste optimisée pour PC.
- Données personnelles et conversations IA conservées localement par défaut.
- Fonctionnement pédagogique minimal sans connexion Internet.
- Sauvegarde et restauration de la base de données.
- Temps de réponse inférieur à deux secondes hors compilation et génération IA.
- Limites strictes de temps, mémoire, disque et réseau pour le code exécuté.
- Journalisation des erreurs sans enregistrer de secrets.
- Accessibilité clavier et contraste lisible.
- Architecture modulaire permettant ultérieurement plusieurs utilisateurs.

## 16. MVP recommandé

La première version exploitable doit contenir :

1. profil utilisateur unique ;
2. tableau de bord simple ;
3. parcours Java et six premiers laboratoires complets ;
4. éditeur intégré et import de solution ;
5. tests automatiques Java isolés ;
6. quiz, réponses libres et checklist ;
7. suivi des scores, du temps et de la progression ;
8. intégration Ollama pour explication et correction ;
9. calendrier et révisions simples ;
10. fondations permettant d'ajouter Python et TypeScript sans refonte.

## 17. Découpage de réalisation

### Phase 1 — Fondations

- initialisation Spring Boot, Angular et PostgreSQL ;
- modèle de données et migrations ;
- catalogue des parcours et affichage d'un laboratoire ;
- suivi des tentatives et progression.

### Phase 2 — Apprentissage Java

- éditeur, import et exécution sécurisée ;
- tests et quiz ;
- six laboratoires Java complets ;
- première version du tableau de bord.

### Phase 3 — Tuteur et révision

- connexion à Ollama ;
- questions de connexion entre concepts ;
- corrections expliquées ;
- répétition espacée et concepts fragiles.

### Phase 4 — Expérience complète

- 24 activités Java ;
- gamification, calendrier et statistiques ;
- projet et défi adaptatifs.

### Phase 5 — Extension multilangage

- worker Python et 24 activités Python ;
- worker TypeScript et 24 activités TypeScript ;
- comparaisons systématiques avec Java.

## 18. Critères de réussite de la V1

- Une activité peut être commencée, sauvegardée, testée et terminée.
- Le score combine correctement tests, quiz, pratique et auto-évaluation.
- Un score insuffisant autorise la suite tout en créant une révision.
- Ollama peut expliquer une erreur en utilisant le contexte du laboratoire.
- L'application affiche progression, calendrier, série, XP et concepts fragiles.
- Une solution écrite dans un IDE peut être importée.
- Le code soumis ne peut pas accéder librement au système hôte ou au réseau.
- Les résultats restent disponibles après redémarrage.

## 19. Décisions validées

- application web responsive ;
- utilisateur unique pour la V1 ;
- ordre Java → Python → TypeScript ;
- 24 activités par langage ;
- programme fixe conçu avec ChatGPT et enrichi par Ollama ;
- code dans l'application et dans un IDE externe ;
- évaluations multiples et correction IA ;
- progression autorisée sous le seuil recommandé ;
- seuil variable selon la difficulté ;
- stack Spring Boot + Angular + PostgreSQL ;
- Ollama installé localement ;
- niveau final professionnel orienté emploi ;
- projet professionnel personnalisé au laboratoire 23 ;
- défi personnalisé selon les faiblesses au laboratoire 24 ;
- statistiques, calendrier, séries, badges, réussites, niveaux et XP ;
- cadence cible de 3 à 4 mois avec au moins une heure quotidienne.

## 20. Points à préciser lors de la conception détaillée

- modèle Ollama retenu selon la puissance du PC ;
- politique exacte de stockage du code et des conversations ;
- choix de l'éditeur embarqué, probablement Monaco Editor ;
- format d'import depuis IntelliJ, VS Code ou PyCharm ;
- thèmes exacts des trois projets professionnels ;
- catalogue final des badges et barème d'XP ;
- limites matérielles des conteneurs d'exécution ;
- maquettes des écrans principaux ;
- stratégie de sauvegarde et d'export des données.

---

## 21. Version 2 — Professeur IA personnel et adaptatif

### 21.1 Vision

La V2 transforme DLR en professeur IA personnel capable d'adapter la formation, de synchroniser la progression entre les appareils et de convertir les compétences maîtrisées en projets professionnels présentables.

DLR reste une application **strictement personnelle et mono-utilisateur**. Elle ne devient ni un réseau social, ni une plateforme pour formateurs, ni une application de simulation d'entretien.

### 21.2 Synchronisation personnelle

- profil utilisateur unique ;
- synchronisation sécurisée entre PC, tablette et téléphone ;
- sauvegarde cloud automatique et restauration ;
- reprise d'une séance sur un autre appareil ;
- fonctionnement hors ligne avec synchronisation différée ;
- historique et préférences cohérents sur tous les appareils.

### 21.3 Parcours adaptatif avancé

- difficulté ajustée selon les résultats et l'autonomie ;
- exercices supplémentaires sur les concepts fragiles ;
- accélération des notions déjà maîtrisées ;
- questions reliant plusieurs anciens concepts ;
- comparaisons ciblées entre Java, Python et TypeScript ;
- révisions adaptées aux erreurs, à l'oubli et à la réutilisation ;
- justification visible de chaque adaptation proposée par l'IA.

Le programme officiel reste fixe. L'IA adapte les exercices, les explications, l'ordre des révisions et la profondeur, sans supprimer silencieusement des compétences obligatoires.

### 21.4 Professeur IA avancé

L'IA assume cinq rôles :

- **professeur** : explique et vérifie la compréhension ;
- **coach** : fournit des indices progressifs ;
- **reviewer** : analyse le code et propose des améliorations ;
- **client fictif** : exprime des besoins métier réalistes ;
- **Tech Lead** : challenge l'architecture et les décisions techniques.

Le rôle de recruteur et toute simulation d'entretien sont exclus, car cette fonction est déjà couverte par une autre application.

### 21.5 Portfolio automatique

- sélection des meilleurs projets et travaux ;
- génération d'un README professionnel ;
- présentation des compétences démontrées ;
- historique synthétique des décisions techniques ;
- préparation de captures, démonstrations et fiches projet ;
- export vers GitHub ;
- page ou dossier partageable sans scores, notes privées ni conversations IA.

### 21.6 Partage ponctuel sans mentorat

- export d'un laboratoire, d'une solution ou d'un projet ;
- création d'un rapport partageable ;
- import d'une autre solution pour comparaison ;
- revue complète par l'IA ;
- aucun tableau de suivi pour mentor ;
- aucun compte formateur ou espace collaboratif permanent.

### 21.7 Nouveaux parcours

La V2 pourra ajouter des parcours spécialisés :

- SQL avancé et optimisation ;
- Spring Boot avancé ;
- Angular avancé ;
- tests professionnels ;
- Docker et CI/CD ;
- microservices ;
- Kafka ;
- AWS ou Azure ;
- algorithmique et structures de données ;
- intelligence artificielle avec Python.

Un parcours long pourra reprendre le format 22 laboratoires + 1 projet + 1 défi. Un parcours ciblé pourra employer un format plus court si ses objectifs le justifient.

### 21.8 Projets professionnels combinés

- projets Spring Boot + Angular ;
- projets FastAPI + Angular ;
- projets Java + Python ;
- API REST, PostgreSQL, Docker et CI/CD ;
- microservices et messagerie ;
- cahiers des charges inspirés de situations professionnelles ;
- sélection du projet selon les compétences à renforcer et le portfolio existant.

### 21.9 Analyse intelligente de la progression

La V2 distingue mémorisation, compréhension, application et maîtrise. Elle peut :

- détecter les blocages et leurs causes probables ;
- prévoir le temps restant ;
- analyser la dépendance aux indices ;
- repérer les concepts fragiles et leurs connexions ;
- mesurer la capacité à transférer un concept dans un nouveau contexte ;
- recommander le meilleur prochain effort ;
- estimer l'autonomie sur un sujet ;
- expliquer les raisons de chaque recommandation.

### 21.10 Exclusions explicites de la V2

- pas de comptes multiples ;
- pas de classement social ;
- pas d'espace mentor ou formateur ;
- pas de tableau de suivi pour une autre personne ;
- pas de simulation d'entretien ;
- pas de publication des données pédagogiques privées.

### 21.11 Critères de réussite de la V2

- la progression se synchronise sans doublon entre plusieurs appareils ;
- les adaptations améliorent réellement la maîtrise sans contourner le programme ;
- l'IA explique et justifie ses recommandations ;
- le portfolio est exportable et ne divulgue aucune donnée privée ;
- les nouveaux parcours réutilisent le moteur pédagogique existant ;
- le fonctionnement hors ligne reste fiable ;
- l'utilisateur conserve le contrôle sur l'IA, les données et les synchronisations.

## 22. Décisions reportées non bloquantes

Les choix suivants seront pris au moment de leur implémentation : fournisseur de synchronisation cloud, modèle Ollama exact, règles détaillées des nouveaux parcours, format du portfolio public et identité visuelle finale. Ils ne bloquent ni le Sprint 0 ni le développement de la V1.

## Conclusion

DLR n'est pas seulement un lecteur de cours. Il constitue un environnement personnel d'entraînement professionnel : apprendre, pratiquer, expliquer, tester, réviser et construire. Java sert de socle cognitif ; Python et TypeScript deviennent plus accessibles grâce aux comparaisons systématiques. Les évaluations déterministes mesurent la justesse, tandis qu'Ollama personnalise les explications et les défis.
