-- Quiz de départ : quelques questions posées avant le cours, sans correction ni effet sur le score.
-- Rattaché au laboratoire (et non à la tentative) car il est proposé avant même qu'une tentative existe.
create table lab_pretest (
    lab_code varchar(40) primary key,
    status varchar(16) not null check (status in ('ANSWERED', 'SKIPPED')),
    completed_at timestamp with time zone not null
);

-- selected_choice vide = « Je ne sais pas ».
create table lab_pretest_answer (
    id uuid primary key,
    lab_code varchar(40) not null,
    question_id varchar(80) not null,
    selected_choice integer,
    answered_at timestamp with time zone not null,
    constraint uq_lab_pretest_answer unique (lab_code, question_id)
);
