create table calendar_notification_configuration
(
    id           bigserial primary key,
    uuid         varchar(36)                    not null unique,
    create_date  timestamp                      not null default now(),
    update_date  timestamp,
    active       boolean                        not null default false,
    calendar_id  int8 references calendar (id)  not null,
    tg_source_id int8 references tg_source (id) not null
);
