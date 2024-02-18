alter table tg_source
    add column kicked boolean;

update tg_source
set kicked = false;

alter table tg_source
    alter column kicked set not null;

alter table tg_source
    alter column kicked set default false;
