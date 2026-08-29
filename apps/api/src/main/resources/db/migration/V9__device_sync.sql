create table sync_device (
    id uuid primary key,
    name varchar(80) not null,
    token_hash varchar(64) not null unique,
    status varchar(16) not null,
    paired_at timestamp with time zone not null,
    last_seen_at timestamp with time zone not null
);

create table sync_change (
    operation_id uuid primary key,
    device_id uuid not null references sync_device(id),
    entity_type varchar(40) not null,
    entity_id varchar(120) not null,
    entity_version bigint not null,
    logical_time bigint not null unique,
    content_hash varchar(64) not null,
    payload_json text not null,
    client_modified_at timestamp with time zone not null,
    created_at timestamp with time zone not null
);

create index idx_sync_change_cursor on sync_change(logical_time);
create index idx_sync_change_entity on sync_change(entity_type, entity_id, entity_version);

create table sync_conflict (
    id uuid primary key,
    entity_type varchar(40) not null,
    entity_id varchar(120) not null,
    entity_version bigint not null,
    existing_operation_id uuid not null references sync_change(operation_id),
    incoming_operation_id uuid not null references sync_change(operation_id),
    detected_at timestamp with time zone not null,
    resolved_at timestamp with time zone
);

create index idx_sync_conflict_unresolved on sync_conflict(resolved_at, detected_at);
