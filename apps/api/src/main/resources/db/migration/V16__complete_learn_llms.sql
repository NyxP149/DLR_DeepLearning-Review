-- Generated from infrastructure/scripts/generate-llm-curriculum.mjs.
update learning_path set status = 'AVAILABLE' where language = 'LEARN_LLM';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select new_lab.id, path.id, new_lab.number, new_lab.slug, new_lab.title, new_lab.difficulty, new_lab.threshold, new_lab.type
from learning_path path
cross join (values
    ('LLM-02', 2, 'temperature-top-k-et-echantillonnage-controle', 'Température, top-k et échantillonnage contrôlé', 'BASE', 70, 'LAB'),
    ('LLM-03', 3, 'templates-de-prompts-et-separation-des-donnees', 'Templates de prompts et séparation des données', 'BASE', 70, 'LAB'),
    ('LLM-04', 4, 'sorties-structurees-et-validation-de-schema', 'Sorties structurées et validation de schéma', 'BASE', 70, 'LAB'),
    ('LLM-05', 5, 'jeux-d-evaluation-et-metriques-utiles', 'Jeux d’évaluation et métriques utiles', 'INTERMEDIAIRE', 70, 'LAB'),
    ('LLM-06', 6, 'embeddings-et-similarite-vectorielle', 'Embeddings et similarité vectorielle', 'INTERMEDIAIRE', 70, 'LAB'),
    ('LLM-07', 7, 'decoupage-chevauchement-et-metadonnees', 'Découpage, chevauchement et métadonnées', 'INTERMEDIAIRE', 70, 'LAB'),
    ('LLM-08', 8, 'recherche-semantique-et-classement', 'Recherche sémantique et classement', 'PROFESSIONNEL', 75, 'LAB'),
    ('LLM-09', 9, 'pipeline-rag-local-et-citations', 'Pipeline RAG local et citations', 'PROFESSIONNEL', 75, 'LAB'),
    ('LLM-10', 10, 'securite-injection-donnees-et-permissions', 'Sécurité : injection, données et permissions', 'PROFESSIONNEL', 75, 'LAB'),
    ('LLM-11', 11, 'projet-professionnel-assistant-rag-local-evalue', 'Projet professionnel : assistant RAG local évalué', 'PROJET', 80, 'PROJECT'),
    ('LLM-12', 12, 'defi-final-comparer-deux-strategies-de-contexte', 'Défi final : comparer deux stratégies de contexte', 'DEFI', 85, 'CHALLENGE')
) as new_lab(id, number, slug, title, difficulty, threshold, type)
where path.language = 'LEARN_LLM';
