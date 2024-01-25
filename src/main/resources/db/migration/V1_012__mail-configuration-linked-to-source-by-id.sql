alter table mail_configuration
    add column tg_source_id int8;

alter table mail_configuration
    add constraint fk_mail_configuration_tg_source_id_2_tg_source_id foreign key (tg_source_id) references tg_source (id);

update mail_configuration
set tg_source_id = ts.id
from mail_configuration mc
         join tg_source ts on ts.source_id = mc.forward_to_telegram_group_id
where mail_configuration.forward_to_telegram_group_id = mc.forward_to_telegram_group_id;

alter table mail_configuration
    drop column forward_to_telegram_group_id;
