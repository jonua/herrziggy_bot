create table mail_configuration
(
    id                           bigserial primary key,
    create_date                  timestamp    not null default now(),
    update_date                  timestamp,
    uuid                         varchar(36)  not null unique,
    active                       boolean      not null default false,
    debug                        boolean      not null default false,
    username                     varchar(100) not null,
    password                     varchar(100) not null,
    store_protocol               varchar(100) not null default 'imaps',
    imaps_host                   varchar(100) not null default 'imap.gmail.com',
    imaps_port                   int4         not null default 993,
    imaps_timeout                int8         not null default 10000,

    zone_id                      varchar(100)          default 'UTC',
    forward_to_telegram_group_id varchar(100)
);
