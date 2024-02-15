alter table calendar
    rename to calendar_configuration;

alter table tg_source
    rename column calendar_id to calendar_configuration_id;

alter table calendar_notification_configuration
    rename column calendar_id to calendar_configuration_id;

alter table calendar_configuration
    add column merge_calendars boolean not null default true;

create table calendar_source
(
    id                 bigserial primary key,
    uuid               varchar(36)   not null unique,
    create_date        timestamp     not null default now(),
    update_date        timestamp,
    name               varchar(50)   not null,
    google_calendar_id varchar(1000) not null
);

create table calendar_configuration_source
(
    calendar_configuration_id int8 not null references calendar_configuration (id),
    calendar_source_id        int8 not null references calendar_source (id)
);

create unique index on calendar_configuration_source (calendar_configuration_id, calendar_source_id);

insert into calendar_source (uuid, create_date, update_date, name, google_calendar_id)
select uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
       now(),
       null,
       cc.additional_info,
       cc.google_calendar_id
from calendar_configuration cc;

insert into calendar_configuration_source
select cc.id,
       cs.id
from calendar_source cs
         join calendar_configuration cc on cc.google_calendar_id = cs.google_calendar_id;

alter table calendar_configuration
    drop column google_calendar_id;
