alter table attempt add column tests_score numeric(5, 2);
alter table attempt add column quiz_score numeric(5, 2);
alter table attempt add column practice_score numeric(5, 2);
alter table attempt add column connections_score numeric(5, 2);
alter table attempt add column self_assessment_score numeric(5, 2);
alter table attempt add column score_version varchar(32);
alter table attempt add column continued_below_threshold boolean not null default false;

create table quiz_answer (
    id uuid primary key,
    attempt_id uuid not null references attempt(id),
    question_id varchar(80) not null,
    selected_choice integer,
    answer_text text,
    score numeric(5, 2) not null,
    feedback varchar(500) not null,
    updated_at timestamp with time zone not null,
    unique (attempt_id, question_id)
);

create table attempt_checklist (
    attempt_id uuid primary key references attempt(id),
    completed_items integer not null check (completed_items >= 0),
    total_items integer not null check (total_items > 0),
    updated_at timestamp with time zone not null
);

create table review_item (
    id uuid primary key,
    attempt_id uuid not null references attempt(id),
    lab_id varchar(40) not null references lab(id),
    due_at timestamp with time zone not null,
    reason varchar(500) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null
);

create index idx_review_due on review_item(status, due_at);
