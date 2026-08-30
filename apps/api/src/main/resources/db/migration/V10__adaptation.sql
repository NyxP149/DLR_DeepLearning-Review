create table adaptation_recommendation (
    id uuid primary key,
    reason varchar(600) not null,
    targeted_concepts varchar(500) not null,
    lab_code varchar(40) not null references lab(id),
    proposed_activity varchar(1200) not null,
    difficulty varchar(32) not null,
    expected_benefit varchar(500) not null,
    factors varchar(1000) not null,
    status varchar(24) not null,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    decided_at timestamp with time zone
);

create index idx_adaptation_active on adaptation_recommendation(status, expires_at);
