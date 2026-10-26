-- Pratique de récupération : confiance déclarée, historique des réponses, indices demandés et révisions par question.

alter table quiz_answer add column confidence smallint;

-- Chaque réponse est journalisée : la première réponse d'une question (avant toute correction) sert au score V2
-- et alimente la calibration confiance / résultat, même si l'apprenant corrige ensuite sa réponse.
create table quiz_answer_log (
    id uuid primary key,
    attempt_id uuid not null references attempt(id) on delete cascade,
    question_id varchar(80) not null,
    selected_choice integer,
    answer_text text,
    score numeric(5, 2) not null,
    confidence smallint,
    answered_at timestamp with time zone not null
);

create index idx_quiz_answer_log_attempt on quiz_answer_log(attempt_id, question_id, answered_at);

-- Niveau d'indice demandé pendant une tentative : la pratique « sans aide » du score V2 en dépend.
create table attempt_hint (
    id uuid primary key,
    attempt_id uuid not null references attempt(id) on delete cascade,
    level smallint not null check (level between 1 and 3),
    created_at timestamp with time zone not null
);

create index idx_attempt_hint_attempt on attempt_hint(attempt_id);

-- Réponse donnée à une question de rappel pendant une révision espacée.
create table review_answer (
    id uuid primary key,
    review_id uuid not null references review_item(id) on delete cascade,
    question_id varchar(80) not null,
    selected_choice integer,
    answer_text text,
    score numeric(5, 2) not null,
    outcome varchar(16) not null,
    confidence smallint,
    answered_at timestamp with time zone not null
);

create index idx_review_answer_review on review_answer(review_id);
