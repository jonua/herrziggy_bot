/*migrated group to supergroup is '-4133004888'*/

insert into calendar (create_date, update_date, uuid, education_type, education_type_description, participation_type, participation_type_description, entering_year, additional_info, google_calendar_id, order_value)
values (now(), now(), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'SECOND', 'Evening', 'EVENING', 'Evening', 2023, '1 group 2023', 'jgb045ro163g4qg89n2n5qt5p8@group.calendar.google.com', 1);

insert into calendar (create_date, update_date, uuid, education_type, education_type_description, participation_type, participation_type_description, entering_year, additional_info, google_calendar_id, order_value)
values (now(), now(), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'SECOND', 'Evening', 'EVENING', 'Evening', 2022, '1 group 2021', 'v9s4tgmo8a8t2i4sn227r8dc2k@group.calendar.google.com', 2);

insert into calendar (create_date, update_date, uuid, education_type, education_type_description, participation_type, participation_type_description, entering_year, additional_info, google_calendar_id, order_value)
values (now(), now(), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'SECOND', 'Evening', 'EVENING', 'Evening', 2022, '1 group 2021', 's16f1e3d9t8p9qb91009gve8ik@group.calendar.google.com', 3);


insert into mail_configuration (create_date, update_date, uuid, active, debug, username, password, zone_id)
values (now(), now(), uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), false, false, 'http418s@gmail.com', 'xapz vpvg ohtu hxfq', 'Europe/Moscow');
