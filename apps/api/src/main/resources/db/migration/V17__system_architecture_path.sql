-- Generated from infrastructure/scripts/generate-system-architecture-curriculum.mjs.
insert into learning_path (language, position, status)
select 'ARCHITECTURE', 9, 'AVAILABLE'
where not exists (select 1 from learning_path where language = 'ARCHITECTURE');

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('ARCHITECTURE-01', 1, 'lire-un-systeme-comme-une-chaine-de-responsabilites', 'Lire un système comme une chaîne de responsabilités', 'BASE', 70, 'LAB'),
    ('ARCHITECTURE-02', 2, 'top-down-du-besoin-utilisateur-aux-contrats', 'Top-down : du besoin utilisateur aux contrats', 'BASE', 70, 'LAB'),
    ('ARCHITECTURE-03', 3, 'bottom-up-des-donnees-vers-le-produit', 'Bottom-up : des données vers le produit', 'BASE', 70, 'LAB'),
    ('ARCHITECTURE-04', 4, 'choisir-une-stack-verticale-avec-une-matrice-de-decision', 'Choisir une stack verticale avec une matrice de décision', 'BASE', 70, 'LAB'),
    ('ARCHITECTURE-05', 5, 'modeliser-les-donnees-et-leurs-invariants', 'Modéliser les données et leurs invariants', 'BASE', 70, 'LAB'),
    ('ARCHITECTURE-06', 6, 'concevoir-api-erreurs-et-idempotence', 'Concevoir API, erreurs et idempotence', 'BASE', 70, 'LAB'),
    ('ARCHITECTURE-07', 7, 'projet-1-application-crud-verticale', 'Projet 1 : application CRUD verticale', 'PROJET', 80, 'PROJECT'),
    ('ARCHITECTURE-08', 8, 'authentification-et-autorisation-de-bout-en-bout', 'Authentification et autorisation de bout en bout', 'INTERMEDIAIRE', 75, 'LAB'),
    ('ARCHITECTURE-09', 9, 'transactions-concurrence-et-coherence', 'Transactions, concurrence et cohérence', 'INTERMEDIAIRE', 75, 'LAB'),
    ('ARCHITECTURE-10', 10, 'valider-toute-la-pyramide-de-tests', 'Valider toute la pyramide de tests', 'INTERMEDIAIRE', 75, 'LAB'),
    ('ARCHITECTURE-11', 11, 'conteneuriser-la-stack-et-ses-dependances', 'Conteneuriser la stack et ses dépendances', 'INTERMEDIAIRE', 75, 'LAB'),
    ('ARCHITECTURE-12', 12, 'deployer-avec-configuration-et-secrets-separes', 'Déployer avec configuration et secrets séparés', 'INTERMEDIAIRE', 75, 'LAB'),
    ('ARCHITECTURE-13', 13, 'decouper-un-monolithe-modulaire', 'Découper un monolithe modulaire', 'INTERMEDIAIRE', 75, 'LAB'),
    ('ARCHITECTURE-14', 14, 'projet-2-systeme-modulaire-securise', 'Projet 2 : système modulaire sécurisé', 'PROJET', 80, 'PROJECT'),
    ('ARCHITECTURE-15', 15, 'mesurer-avant-d-optimiser', 'Mesurer avant d’optimiser', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-16', 16, 'introduire-un-cache-sans-perdre-la-verite', 'Introduire un cache sans perdre la vérité', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-17', 17, 'decoupler-avec-evenements-et-files-de-messages', 'Découpler avec événements et files de messages', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-18', 18, 'stocker-et-distribuer-les-fichiers', 'Stocker et distribuer les fichiers', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-19', 19, 'observer-traces-metriques-et-journaux', 'Observer traces, métriques et journaux', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-20', 20, 'securiser-chaque-frontiere', 'Sécuriser chaque frontière', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-21', 21, 'concevoir-la-resilience-et-la-reprise', 'Concevoir la résilience et la reprise', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-22', 22, 'dimensionner-et-faire-evoluer-la-plateforme', 'Dimensionner et faire évoluer la plateforme', 'PROFESSIONNEL', 80, 'LAB'),
    ('ARCHITECTURE-23', 23, 'projet-professionnel-plateforme-multi-services-observable', 'Projet professionnel : plateforme multi-services observable', 'PROJET', 85, 'PROJECT'),
    ('ARCHITECTURE-24', 24, 'defi-final-architecture-complete-sous-contraintes', 'Défi final : architecture complète sous contraintes', 'DEFI', 90, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'ARCHITECTURE';
