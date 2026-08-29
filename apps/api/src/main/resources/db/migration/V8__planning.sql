create table study_session (
    id uuid primary key,
    session_date date not null unique,
    planned_minutes integer not null check (planned_minutes between 0 and 480),
    actual_minutes integer not null check (actual_minutes between 0 and 480),
    status varchar(32) not null,
    reward varchar(120),
    completed_at timestamp with time zone
);

create index idx_study_session_date on study_session(session_date);
