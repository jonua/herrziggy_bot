create table mail_subscriber
(
    mail_configuration_id int8 not null,
    tg_source_id          int8 not null
);

create unique index mail_subscriber_mail_configuration_id_tg_source_id
    on mail_subscriber (mail_configuration_id, tg_source_id);

update mail_subscriber
set mail_configuration_id = mc.id,
    tg_source_id          = mc.tg_source_id
from mail_configuration mc
where mc.tg_source_id is not null;

alter table mail_configuration
    drop column tg_source_id;
