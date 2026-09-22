# Issues

## La réponse correcte du quiz était exposée par l'API
- severity: high
- date: 2026-08
- tags: Sécurité, API

**Problème**
`GET /api/labs/JAVA-01` sérialisait directement le modèle métier, y compris le champ `correctChoice`, ce qui aurait permis à n'importe quel apprenant de lire la bonne réponse dans la réponse JSON brute.

**Solution**
Ajout de DTOs de sortie dédiés (`LabDetailResponse`, `QuizQuestionResponse`) séparant le contenu affichable des données de correction, avec une assertion MockMvc vérifiant en continu l'absence de `correctChoice` dans la réponse.

---

## Le runner TypeScript dépasse systématiquement son timeout
- severity: medium
- date: 2026-08
- tags: Runner, Performance

**Problème**
Le test Docker réel du runner TypeScript retournait `TIMEOUT` après 10 secondes alors que Java et Python réussissaient : `tsc` revérifiait les déclarations de toute sa bibliothèque standard sous les limites de 128 Mo et 0,5 CPU du conteneur isolé.

**Solution**
Ajout de l'option `--skipLibCheck` tout en conservant `--strict` sur le code de l'apprenant ; l'exécution réelle passe ensuite à environ 5 secondes sans affaiblir la vérification du code soumis.

---

## Les nouveaux laboratoires multilangages faussent la progression Java
- severity: medium
- date: 2026-08
- tags: Dashboard, Données

**Problème**
Après l'ajout des parcours Python, TypeScript et Learn LLMs, le tableau de bord annonçait 9 laboratoires Java au lieu de 6 : le compteur utilisait la taille totale du catalogue au lieu de filtrer par parcours.

**Solution**
Calcul du total, des laboratoires terminés, du badge Java et du prochain laboratoire restreint au sous-ensemble `language = JAVA`, en conservant l'XP comme métrique globale valorisant tous les parcours.

---

## Les activités Spring Boot gonflent le compteur de progression Java
- severity: medium
- date: 2026-08
- tags: Dashboard, Données

**Problème**
Le tableau de bord annonçait 36 laboratoires Java au lieu de 24 : les preuves Spring Boot s'exécutent bien via le runner Java, mais le calcul de progression confondait le langage d'exécution avec le parcours pédagogique.

**Solution**
Sélection du parcours Java par le préfixe de contenu `JAVA-` plutôt que par le runner utilisé, afin que Spring Boot reste exécuté en Java sans contaminer la progression du parcours Java.

---

## Les facteurs d'adaptation exposent des valeurs techniques internes
- severity: low
- date: 2026-08
- tags: UI, Adaptation

**Problème**
L'écran Coach V2 affichait directement des valeurs de domaine comme `CONSOLIDATING` ou une étape de révision `-1` dans une carte destinée à l'apprenant, au lieu d'un texte compréhensible.

**Solution**
Traduction explicite des statuts internes en texte lisible et remplacement de la sentinelle négative par la formulation « aucune révision différée validée ».

---

## Ollama invente une restriction inexistante sur le bytecode
- severity: medium
- date: 2026-08
- tags: IA, Qualité

**Problème**
Lors de la correction qualitative d'une réponse libre, le modèle affirmait à tort qu'un bytecode Java devait être généré pour une plateforme cible précise, alors que la réponse de l'apprenant décrivait correctement sa portabilité — la question et les objectifs seuls ne fournissaient pas une base factuelle assez contraignante.

**Solution**
Injection des sections et concepts publics du laboratoire comme références fiables dans le prompt, interdiction explicite d'inventer un fait absent et obligation d'annoncer les critères réels avant de signaler une erreur.

---

## CORS refuse l'origine locale 127.0.0.1
- severity: low
- date: 2026-08
- tags: CORS, Configuration

**Problème**
Le dashboard affichait « API indisponible » alors que le shell se chargeait correctement : seule l'origine `http://localhost:4200` était autorisée côté API, alors qu'un accès réel utilisait `http://127.0.0.1:4200`.

**Solution**
Autorisation explicite des deux origines locales équivalentes dans la configuration CORS, sans recourir à un joker réseau.

---

## Le calendrier propose des dates indépendantes de la progression réelle
- severity: medium
- date: 2026-08
- tags: Planning

**Problème**
Le planificateur ne connaissait que les séances quotidiennes prévues et ne reliait pas sa projection aux tentatives réellement terminées, ce qui pouvait suggérer des dates futures figées sans lien avec l'avancement effectif de l'apprenant.

**Solution**
La projection lit désormais les tentatives qualifiantes et leur `completed_at` ; après chaque validation, seule la prochaine activité déverrouillée reçoit une date, calculée depuis la fin effective et le rythme semaine/week-end du profil, tandis que les étapes suivantes restent sans date tant que leur prérequis n'est pas validé.

---

## Le Blueprint Render est introuvable au premier déploiement
- severity: medium
- date: 2026-09
- tags: Déploiement, Render

**Problème**
L'écran New Blueprint de Render indiquait `Blueprint file render.yaml not found on main branch` : le guide de déploiement avait été rédigé avant que le socle technique correspondant ne soit livré dans le dépôt.

**Solution**
Ajout du Blueprint racine `render.yaml` déclarant les deux services (`dlr-api`, `dlr-web`) avec liaison automatique de leurs URL publiques respectives.

---

## Le Runner apparaît indisponible en permanence sur Render
- severity: medium
- date: 2026-09
- tags: Déploiement, Runner

**Problème**
La variable `DLR_EXECUTION_AVAILABLE=false` était figée dans le Blueprint Render, affichant en permanence le Runner comme indisponible même lorsque l'exécution locale hybride était réellement opérationnelle.

**Solution**
La configuration autorise désormais la découverte dynamique : l'API exécute `docker image inspect` sur les trois images requises avec timeout et décide de l'état réel du Runner, au lieu de dépendre d'une seule variable statique.

---

## Le curseur Monaco reste bloqué après le changement de thème
- severity: medium
- date: 2026-09
- tags: Éditeur, UI

**Problème**
Une petite zone rectangulaire apparaissait au-dessus du code, le curseur semblait bloqué et les clics devenaient imprévisibles : les règles CSS globales des six thèmes ciblaient toutes les `textarea` avec `!important`, y compris la zone de saisie technique invisible utilisée en interne par Monaco.

**Solution**
Exclusion explicite des zones internes `.inputarea` et `.ime-text-area` des styles de formulaires, restauration de leurs propriétés invisibles et recalcul de la mise en page après le premier rendu, avec un test Playwright de non-régression qui clique et saisit du texte dans l'éditeur.

---

## Création simultanée de plusieurs tentatives lors de l'autosauvegarde des notes
- severity: medium
- date: 2026-09
- tags: Notes, Concurrence

**Problème**
Les autosauvegardes concurrentes du carnet personnel, des réponses et des analyses Ollama pouvaient chacune déclencher leur propre création de tentative en l'absence d'une tentative déjà ouverte, provoquant des doublons.

**Solution**
Les autosauvegardes concurrentes partagent désormais la même promesse de création de tentative, garantissant qu'une seule tentative est créée même si plusieurs champs sont modifiés au même moment.

---

## Une ancienne réponse de réflexion réapparaît après suppression
- severity: low
- date: 2026-09
- tags: Notes, Données

**Problème**
Effacer une réponse de réflexion dans l'interface ne supprimait pas systématiquement sa version déjà persistée côté serveur, ce qui pouvait la faire réapparaître au rechargement du laboratoire.

**Solution**
Ajout d'une route `DELETE` dédiée qui retire explicitement la réponse de la tentative dès que son champ est vidé côté client, garantissant qu'une valeur effacée ne soit jamais restaurée.

---

## Le correctif de l'éditeur n'atteint pas le navigateur à cause du service worker
- severity: medium
- date: 2026-09
- tags: PWA, Éditeur, Cache

**Problème**
Le correctif Monaco était déjà sur `main`, mais l'éditeur restait inutilisable pour l'utilisateur, avec un rectangle violet parasite au clic : le service worker servait tous les fichiers hors navigation en cache d'abord, si bien que les `styles.css` et `main.js` d'avant le correctif, à noms fixes sous `npm start`, restaient servis indéfiniment.

**Solution**
Le cache est renommé `dlr-v3-shell` pour supprimer l'ancien à l'activation, seuls les fichiers au nom haché par leur contenu restent servis depuis le cache, et tout le reste passe par le réseau d'abord avec repli hors ligne ; un rechargement suffit ensuite à récupérer la version corrigée.

---

## Les modifications de code non exécutées sont perdues au rechargement
- severity: medium
- date: 2026-09
- tags: Laboratoire, Brouillon, Données

**Problème**
Après avoir modifié le code d'un laboratoire déjà commencé, un rechargement rendait le code de la dernière exécution : dès qu'un espace de travail serveur existait, le brouillon local était supprimé à l'ouverture sans être comparé au code enregistré.

**Solution**
Le brouillon local est maintenant comparé au code du serveur : identique il est supprimé, différent il est restauré car plus récent. La protection contre une exécution lancée pendant le chargement dépend de la présence d'un espace de travail et non de code déjà soumis, une tentative pouvant exister sans soumission.

---

## L'éditeur Monaco reste inutilisable sur certains postes
- severity: medium
- date: 2026-09
- tags: Éditeur, Accessibilité

**Problème**
Sur le poste de l'utilisateur, le texte du laboratoire restait impossible à modifier et un rectangle parasite s'affichait au clic, sans que la cause ait pu être reproduite : Monaco fonctionne dans les deux modes de saisie testés et le cache périmé du service worker n'expliquait pas tout.

**Solution**
Ajout d'un éditeur simple à champ texte natif activable d'un clic et mémorisé, d'un téléchargement du fichier pour l'éditer dans un IDE puis le réimporter, et de tests Playwright qui couvrent ces deux chemins. La cause exacte côté Monaco reste à établir.

---

## Le curseur Monaco ne se déplace pas librement et un rectangle vide s'affiche
- severity: high
- date: 2026-09
- tags: Éditeur, Compatibilité, Dépendances

**Problème**
Sur le poste de l'utilisateur, l'éditeur Monaco empêchait le curseur de se déplacer librement et affichait un rectangle violet vide au début de la zone de code, alors qu'il fonctionnait dans tous les essais faits ailleurs, avec ses deux modes de saisie : sa saisie repose sur un élément caché ou sur l'API EditContext, sensible aux styles globaux et au navigateur, et la cause précise n'a pas pu être isolée.

**Solution**
Monaco est remplacé par CodeMirror 6, qui édite un élément `contenteditable` standard et se thème directement avec les variables CSS de l'application. Le curseur, la sélection et la suppression sont couverts par des tests Playwright au clavier réel, et l'application entière tient désormais en environ 1 Mo de JavaScript.

---

## La réponse du tuteur Ollama semble coupée
- severity: low
- date: 2026-09
- tags: UX, Tuteur IA

**Problème**
L'utilisateur signale que la fenêtre de réponse du tuteur Ollama est petite et coupe la réponse. Aucun découpage CSS n'existait réellement : la colonne de droite atteint environ 3142 px pour une réponse longue, ce qui oblige à faire défiler toute la page pour en lire la fin, perçu par l'utilisateur comme une fenêtre trop petite.

**Solution**
`.tutor-answer` reçoit une hauteur maximale (`min(28rem, 55vh)`) et son propre défilement interne stylé aux couleurs de l'application, au lieu de dépendre du défilement de la page entière. Vérifié en injectant une réponse longue simulée via `window.ng.getComponent()`.

---

## Le bilan de fin de laboratoire disparaît au rechargement de la page
- severity: high
- date: 2026-09
- tags: Backend, Données, Régression fonctionnelle

**Problème**
L'utilisateur signale qu'après avoir cliqué sur « Terminer et calculer mon score », rien ne semble sauvegardé. En vérifiant le cycle de vie complet d'une tentative (démarrage, exécution, quiz, checklist, complétion) via l'API puis dans le navigateur, `AttemptService.current(labCode)` s'est révélé ne chercher que la tentative **en cours** : une fois complétée, son statut passe à `COMPLETED` et sort de ce filtre, donc au rechargement de la page l'API renvoie « aucune tentative » et le frontend réinitialise tout (code, quiz, checklist, bilan) à l'état vierge — alors que le score est réellement écrit en base.

**Solution**
`AttemptRepository.findLatestInProgress` est remplacé par `findLatest`, qui renvoie la tentative la plus récente quel que soit son statut ; le record `Attempt` expose désormais aussi les scores détaillés déjà stockés en base. Le frontend reconstruit le panneau de bilan à partir de cette tentative dès qu'elle n'est plus en cours. Couvert par un nouveau test Playwright qui termine réellement un laboratoire puis recharge la page.

---

## Le tuteur Ollama répond très lentement
- severity: medium
- date: 2026-09
- tags: Performance, Tuteur IA

**Problème**
L'utilisateur signale des réponses très lentes du professeur Ollama. Le modèle par défaut, `llama3.1:latest` (8 Md de paramètres), est lourd pour une inférence CPU locale : le poste de développement n'a pas de GPU exploité par Ollama (`size_vram: 0`, seul un Intel Iris Xe intégré est présent).

**Solution**
Le modèle par défaut passe à `llama3.2:latest` (3,2 Md de paramètres, déjà installé localement), toujours configurable via `DLR_OLLAMA_MODEL`. Le gain est net (66 s à froid puis ~32 s à chaud contre nettement plus avec le modèle 8B) sans être instantané, l'inférence restant limitée par l'absence de GPU.

---

## Un test d'exercice semble refuser un résultat pourtant correct
- severity: low
- date: 2026-09
- tags: Validation, Pédagogie, UX

**Problème**
L'utilisateur signale qu'un résultat logiquement correct mais formaté différemment (ex. `total = 42` au lieu de `total=42`) est marqué comme incorrect, et demande un audit des laboratoires. Vérification faite : la comparaison stricte de la sortie du programme est un choix pédagogique assumé sur tout le catalogue — 124 des 148 exercices demandent explicitement dans leur consigne d'« afficher exactement » une chaîne précise — mais le message d'échec générique ne montrait pas la sortie attendue, rendant l'écart de formatage difficile à repérer.

**Solution**
Le message d'échec du test visible affiche désormais la sortie attendue à côté de la sortie obtenue (`ExecutionService.run`), au lieu d'une phrase générique. La logique de comparaison elle-même n'a pas changé : elle reste volontairement exacte.
