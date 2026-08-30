insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('JAVA-07', 7, 'heritage-composition-polymorphisme', 'Héritage, composition et polymorphisme', 'INTERMEDIAIRE', 70, 'LAB'),
    ('JAVA-08', 8, 'interfaces-classes-abstraites-contrats', 'Interfaces, classes abstraites et contrats', 'INTERMEDIAIRE', 70, 'LAB'),
    ('JAVA-09', 9, 'exceptions-validation-erreurs', 'Exceptions, validation et gestion des erreurs', 'INTERMEDIAIRE', 70, 'LAB'),
    ('JAVA-10', 10, 'collections-egalite-tri', 'Collections, égalité, tri et comparateurs', 'INTERMEDIAIRE', 70, 'LAB'),
    ('JAVA-11', 11, 'generiques-reutilisabilite', 'Génériques et programmation réutilisable', 'INTERMEDIAIRE', 70, 'LAB'),
    ('JAVA-12', 12, 'lambdas-streams', 'Lambdas, interfaces fonctionnelles et Streams', 'INTERMEDIAIRE', 70, 'LAB'),
    ('JAVA-13', 13, 'fichiers-dates-api-standard', 'Fichiers, sérialisation, dates et API standard', 'INTERMEDIAIRE', 70, 'LAB'),
    ('JAVA-14', 14, 'immutabilite-records-optional', 'Immutabilité, records, Optional et bonnes pratiques', 'AVANCE', 75, 'LAB'),
    ('JAVA-15', 15, 'tests-junit-mockito-strategie', 'Tests avec JUnit, Mockito et stratégie de test', 'AVANCE', 75, 'LAB'),
    ('JAVA-16', 16, 'solid-clean-code-refactoring', 'SOLID, clean code et refactoring', 'AVANCE', 75, 'LAB'),
    ('JAVA-17', 17, 'patterns-strategy-factory-builder-template', 'Design patterns : Strategy, Factory, Builder et Template', 'AVANCE', 75, 'LAB'),
    ('JAVA-18', 18, 'concurrence-executeurs-synchronisation', 'Concurrence, threads, exécuteurs et synchronisation', 'AVANCE', 75, 'LAB'),
    ('JAVA-19', 19, 'jdbc-sql-transactions', 'JDBC, SQL, transactions et accès aux données', 'PROFESSIONNEL', 75, 'LAB'),
    ('JAVA-20', 20, 'spring-boot-injection-architecture', 'Spring Boot, injection de dépendances et architecture en couches', 'PROFESSIONNEL', 75, 'LAB'),
    ('JAVA-21', 21, 'rest-dto-validation-jpa-securite', 'API REST, DTO, validation, JPA/Hibernate et sécurité de base', 'PROFESSIONNEL', 75, 'LAB'),
    ('JAVA-22', 22, 'performance-observabilite-docker-entretien', 'Performance, observabilité, Docker et préparation entretien', 'PROFESSIONNEL', 80, 'LAB'),
    ('JAVA-23', 23, 'projet-professionnel-commandes', 'Projet professionnel : moteur de commandes', 'PROJET', 80, 'PROJECT'),
    ('JAVA-24', 24, 'defi-final-adaptatif', 'Défi final Java personnalisé', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'JAVA';
