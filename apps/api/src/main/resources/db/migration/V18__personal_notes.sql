create table lab_note (
    profile_id bigint not null references user_profile(id),
    lab_id varchar(40) not null references lab(id),
    content text not null,
    updated_at timestamp with time zone not null,
    primary key (profile_id, lab_id)
);

create index idx_lab_note_updated on lab_note(updated_at desc);

create table reflection_analysis (
    profile_id bigint not null references user_profile(id),
    lab_id varchar(40) not null references lab(id),
    question_id varchar(80) not null,
    content text not null,
    updated_at timestamp with time zone not null,
    primary key (profile_id, lab_id, question_id)
);
