-- Brouillon du formulaire « Nouvelle fiche projet » : une seule ligne, l'application est mono-utilisateur.
create table portfolio_draft (
    id integer primary key check (id = 1),
    title varchar(160) not null,
    summary varchar(1200) not null,
    source_lab_codes varchar(4000) not null,
    decisions varchar(4000) not null,
    updated_at timestamp with time zone not null
);
