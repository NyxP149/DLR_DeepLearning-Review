create table submission (
    id uuid primary key,
    attempt_id uuid not null references attempt(id),
    language varchar(32) not null,
    source_code text not null,
    origin varchar(32) not null,
    created_at timestamp with time zone not null
);

create index idx_submission_attempt on submission(attempt_id);

create table execution_result (
    id uuid primary key,
    submission_id uuid not null references submission(id),
    status varchar(40) not null,
    exit_code integer,
    standard_output text not null,
    error_output text not null,
    duration_ms bigint not null,
    created_at timestamp with time zone not null
);

create index idx_execution_submission on execution_result(submission_id);

