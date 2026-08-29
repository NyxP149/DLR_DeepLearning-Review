alter table attempt_checklist add column completed_state varchar(500) not null default '';
alter table review_item add column repetition_stage integer not null default 0;
alter table review_item add column completed_at timestamp with time zone;
