create table tg_source
(
    id          bigserial primary key,
    create_date timestamp    not null default now(),
    update_date timestamp,
    uuid        varchar(36)  not null unique,
    source_id   varchar(100) not null,
    type        varchar(100),
    first_name  varchar(100),
    last_name   varchar(100),
    username    varchar(100),
    title       varchar(100)
);

create index on tg_source (source_id);
