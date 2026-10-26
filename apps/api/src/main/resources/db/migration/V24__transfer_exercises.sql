-- Exercices de transfert : une soumission peut viser un exercice de transfert d'un laboratoire.
-- Vide = l'exercice guidé du laboratoire (toutes les soumissions existantes).
alter table submission add column exercise_code varchar(60);

create index idx_submission_exercise on submission(attempt_id, exercise_code);
