create table tg_source_calendar
(
    tg_source_id int8 not null references tg_source (id),
    calendar_id  int8 not null references calendar (id)
);

create unique index on tg_source_calendar (tg_source_id, calendar_id);

update tg_source_calendar
set tg_source_id = s.id,
    calendar_id  = c.id
from tg_source s
         join calendar c on c.id = s.calendar_id;

alter table tg_source
    drop column calendar_id;

alter table calendar_notification_configuration
    drop column calendar_id;
