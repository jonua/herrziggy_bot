create table calendar
(
    id                             bigserial primary key,
    create_date                    timestamp     not null default now(),
    update_date                    timestamp,
    uuid                           varchar(36)   not null unique,
    education_type                 varchar(100)  not null, /* MASTER_S, SECOND, ADDITIONAL */
    education_type_description     varchar(1000) not null,
    participation_type             varchar(100)  not null, /* FULL_TIME, EVENING, PART_TIME */
    participation_type_description varchar(1000) not null,
    entering_year                  int4          not null,
    additional_info                varchar(1000),
    google_calendar_id             varchar(100)
);

alter table tg_user
    add column calendar_id int8;
