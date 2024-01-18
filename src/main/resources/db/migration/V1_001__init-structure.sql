create table tg_user
(
    id          bigserial primary key,
    create_date timestamp    not null default now(),
    update_date timestamp,
    uuid        varchar(36)  not null unique,
    first_name  varchar(100),
    last_name   varchar(100),
    user_id     varchar(100) not null,
    username    varchar(100)
);

create index on tg_user (user_id);
