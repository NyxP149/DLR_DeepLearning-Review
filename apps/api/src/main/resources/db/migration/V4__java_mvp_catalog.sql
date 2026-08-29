insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select code, path.id, number, slug, title, 'BASE', 70, 'LAB'
from learning_path path
cross join (values
    ('JAVA-02', 2, 'variables-types-operateurs', 'Variables, types primitifs, opérateurs et conversions'),
    ('JAVA-03', 3, 'conditions-boucles-algorithmes', 'Conditions, boucles et raisonnement algorithmique'),
    ('JAVA-04', 4, 'methodes-parametres-portee', 'Méthodes, paramètres, retour, portée et surcharge'),
    ('JAVA-05', 5, 'tableaux-chaines-donnees', 'Tableaux, chaînes et manipulation de données'),
    ('JAVA-06', 6, 'classes-objets-encapsulation', 'Classes, objets, constructeurs et encapsulation')
) as activity(code, number, slug, title)
where path.language = 'JAVA';
