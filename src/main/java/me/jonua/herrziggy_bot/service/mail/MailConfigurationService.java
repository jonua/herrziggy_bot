package me.jonua.herrziggy_bot.service.mail;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.data.jpa.repository.MailConfigurationRepository;
import me.jonua.herrziggy_bot.mail.MailConfiguration;
import me.jonua.herrziggy_bot.model.BaseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MailConfigurationService {
    private final MailConfigurationRepository mailConfigurationRepository;

    @Transactional
    public List<MailConfiguration> getActiveConfigurations() {
        Map<Object, List<MailConfiguration>> groupedByUuid = mailConfigurationRepository.findAllActive().stream().collect(Collectors.groupingBy(BaseEntity::getUuid));
        return groupedByUuid.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Transactional
    public void updateLastUse(String mailConfigUuid) {
        mailConfigurationRepository.updateLastUse(mailConfigUuid, new Date());
    }
}
