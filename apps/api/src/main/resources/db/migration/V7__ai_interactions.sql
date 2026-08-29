create table ai_interaction (
    id uuid primary key,
    purpose varchar(40) not null,
    model varchar(120) not null,
    prompt_version varchar(32) not null,
    input_hash varchar(64) not null,
    response text not null,
    created_at timestamp with time zone not null
);

create index idx_ai_interaction_created on ai_interaction(created_at);
