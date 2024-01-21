alter table tg_source add column calendar_id int8;
alter table tg_source
    add constraint fk_tg_source_calendar_id_2_calendar_id
        foreign key (calendar_id) references calendar (id);
