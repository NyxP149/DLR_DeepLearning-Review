-- Parcours « Terminal et IA en ligne de commande » : 24 laboratoires bash et PowerShell.
-- Le contenu pédagogique vit dans content/terminal/*.json ; cette migration ne déclare que les lignes de suivi.
insert into learning_path (language, position, status)
select 'TERMINAL', 10, 'AVAILABLE'
where not exists (select 1 from learning_path where language = 'TERMINAL');

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('TERMINAL-01', 1, 'se-reperer-dans-le-systeme-de-fichiers', 'Se repérer dans le système de fichiers', 'BASE', 70, 'LAB'),
    ('TERMINAL-02', 2, 'creer-copier-deplacer-et-supprimer-des-fichiers', 'Créer, copier, déplacer et supprimer des fichiers', 'BASE', 70, 'LAB'),
    ('TERMINAL-03', 3, 'lire-chercher-et-compter-dans-des-fichiers', 'Lire, chercher et compter dans des fichiers', 'BASE', 70, 'LAB'),
    ('TERMINAL-04', 4, 'redirections-et-pipes-relier-les-commandes', 'Redirections et pipes : relier les commandes', 'BASE', 70, 'LAB'),
    ('TERMINAL-05', 5, 'permissions-et-execution-de-scripts', 'Permissions et exécution de scripts', 'BASE', 70, 'LAB'),
    ('TERMINAL-06', 6, 'variables-d-environnement-path-et-guillemets', 'Variables d''environnement, PATH et guillemets', 'BASE', 70, 'LAB'),
    ('TERMINAL-07', 7, 'projet-1-ranger-un-dossier-de-projet-en-desordre', 'Projet 1 : ranger un dossier de projet en désordre', 'PROJET', 80, 'PROJECT'),
    ('TERMINAL-08', 8, 'premiers-pas-avec-powershell-cmdlets-aide-et-alias', 'Premiers pas avec PowerShell : cmdlets, aide et alias', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-09', 9, 'objets-et-pipeline-powershell-filtrer-trier-grouper', 'Objets et pipeline PowerShell : filtrer, trier, grouper', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-10', 10, 'fichiers-et-texte-avec-powershell', 'Fichiers et texte avec PowerShell', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-11', 11, 'variables-d-environnement-path-et-profil-sous-windows', 'Variables d''environnement, PATH et profil sous Windows', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-12', 12, 'ecrire-des-fonctions-et-des-scripts-powershell-fiables', 'Écrire des fonctions et des scripts PowerShell fiables', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-13', 13, 'ecrire-des-scripts-bash-robustes', 'Écrire des scripts bash robustes', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-14', 14, 'traiter-du-texte-sort-cut-awk-sed-et-jq', 'Traiter du texte : sort, cut, awk, sed et jq', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-15', 15, 'git-en-terminal-suivre-l-historique-d-un-projet', 'Git en terminal : suivre l''historique d''un projet', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-16', 16, 'branches-fusions-et-conflits-avec-git', 'Branches, fusions et conflits avec Git', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-17', 17, 'automatiser-un-projet-avec-make-et-des-scripts', 'Automatiser un projet avec make et des scripts', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-18', 18, 'passer-de-bash-a-powershell-equivalences-et-scripts-portables', 'Passer de bash à PowerShell : équivalences et scripts portables', 'INTERMEDIAIRE', 75, 'LAB'),
    ('TERMINAL-19', 19, 'piloter-un-assistant-ia-en-terminal-boucle-d-agent-et-taches-bien-cadrees', 'Piloter un assistant IA en terminal : boucle d''agent et tâches bien cadrées', 'PROFESSIONNEL', 80, 'LAB'),
    ('TERMINAL-20', 20, 'donner-du-contexte-durable-claude-md-et-conventions-de-depot', 'Donner du contexte durable : CLAUDE.md et conventions de dépôt', 'PROFESSIONNEL', 80, 'LAB'),
    ('TERMINAL-21', 21, 'permissions-et-garde-fous-pour-un-assistant-ia', 'Permissions et garde-fous pour un assistant IA', 'PROFESSIONNEL', 80, 'LAB'),
    ('TERMINAL-22', 22, 'reviser-le-travail-de-l-ia-diffs-tests-et-commits-atomiques', 'Réviser le travail de l''IA : diffs, tests et commits atomiques', 'PROFESSIONNEL', 80, 'LAB'),
    ('TERMINAL-23', 23, 'projet-professionnel-piloter-un-projet-avec-un-assistant-ia-et-des-garde-fous', 'Projet professionnel : piloter un projet avec un assistant IA et des garde-fous', 'PROJET', 85, 'PROJECT'),
    ('TERMINAL-24', 24, 'defi-final-reprendre-un-projet-en-desordre-sous-garde-fous', 'Défi final : reprendre un projet en désordre sous garde-fous', 'DEFI', 90, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'TERMINAL';
