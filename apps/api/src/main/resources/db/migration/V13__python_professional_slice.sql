insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('PYTHON-02', 2, 'fonctions-contrats-erreurs', 'Fonctions, contrats et gestion des erreurs', 'BASE', 70, 'LAB'),
    ('PYTHON-03', 3, 'collections-comprehensions', 'Collections, compréhensions et transformations', 'BASE', 70, 'LAB'),
    ('PYTHON-04', 4, 'fichiers-json-contextes', 'Fichiers, JSON et gestionnaires de contexte', 'INTERMEDIAIRE', 70, 'LAB'),
    ('PYTHON-05', 5, 'objets-dataclasses-tests', 'Objets, dataclasses et invariants testables', 'INTERMEDIAIRE', 75, 'LAB'),
    ('PYTHON-06', 6, 'projet-pipeline-commandes', 'Projet intermédiaire : pipeline de commandes', 'PROJET', 75, 'PROJECT')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'PYTHON';
