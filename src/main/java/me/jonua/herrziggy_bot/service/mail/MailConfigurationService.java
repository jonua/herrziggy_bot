package me.jonua.herrziggy_bot.service.mail;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.data.jpa.repository.MailConfigurationRepository;
import me.jonua.herrziggy_bot.mail.MailConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailConfigurationService {
    private final MailConfigurationRepository mailConfigurationRepository;

    @Transactional
    public List<MailConfiguration> getActiveConfigurations() {
        return mailConfigurationRepository.findAllActive();
    }
}
