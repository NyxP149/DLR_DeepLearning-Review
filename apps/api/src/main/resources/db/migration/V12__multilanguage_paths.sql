update learning_path set status = 'BETA' where language in ('PYTHON', 'TYPESCRIPT');

insert into learning_path (language, position, status)
values ('LEARN_LLM', 4, 'BETA');

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select 'PYTHON-01', id, 1, 'python-depuis-java', 'Python depuis Java : syntaxe, types et collections', 'BASE', 70, 'LAB'
from learning_path where language = 'PYTHON';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select 'TYPESCRIPT-01', id, 1, 'typescript-depuis-java', 'TypeScript depuis Java : types et compilation', 'BASE', 70, 'LAB'
from learning_path where language = 'TYPESCRIPT';

insert into lab (id, path_id, number, slug, title, difficulty, threshold, type)
select 'LLM-01', id, 1, 'premier-prompt-mesurable', 'Learn LLMs : prompt, tokens et sortie contrôlée', 'BASE', 70, 'LAB'
from learning_path where language = 'LEARN_LLM';
