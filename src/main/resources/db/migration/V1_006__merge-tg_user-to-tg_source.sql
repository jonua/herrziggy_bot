alter table tg_source add column is_bot boolean default false not null;
alter table tg_source add column is_premium boolean default false not null;

insert into tg_source (create_date, update_date, uuid, source_id, type, first_name, last_name, username, title, calendar_id, is_bot, is_premium)
select u.create_date,
       u.update_date,
       u.uuid,
       u.user_id,
       'private',
       u.first_name,
       u.last_name,
       u.username,
       null,
       u.calendar_id,
       u.is_bot,
       u.is_premium
from tg_user u
         left join tg_source s on s.source_id = u.user_id
where s.id is null;

update tg_source
set
    is_bot = u.is_bot,
    is_premium = u.is_premium,
    calendar_id = u.calendar_id
from tg_user u
where u.user_id = source_id;

drop table tg_user;
