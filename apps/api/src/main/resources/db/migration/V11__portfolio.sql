create table portfolio_project (
    id uuid primary key,
    slug varchar(100) not null unique,
    title varchar(160) not null,
    summary varchar(1200) not null,
    source_lab_codes varchar(500) not null,
    status varchar(24) not null default 'PRIVATE',
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table portfolio_decision (
    id uuid primary key,
    project_id uuid not null references portfolio_project(id) on delete cascade,
    summary varchar(1000) not null,
    position integer not null
);

create table portfolio_export (
    id uuid primary key,
    project_id uuid not null references portfolio_project(id) on delete cascade,
    format varchar(20) not null,
    content_hash varchar(64) not null,
    included_content varchar(500) not null,
    created_at timestamp with time zone not null
);
