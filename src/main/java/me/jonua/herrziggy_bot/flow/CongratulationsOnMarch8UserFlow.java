package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.Gender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.service.StorageService;
import me.jonua.herrziggy_bot.service.admin.AdminCommandService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CongratulationsOnMarch8UserFlow implements UserFlow {
    private final StorageService storageService;
    private final MessageSender messageSender;
    private final AdminCommandService adminCommandService;
    @Value("${messages.congratulation-on-march-8-message}")
    private String congratulationText;
    @Value("${bot.admin-telegram-id}")
    private Long adminTgId;

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.CONGRATULATION_ON_8_MARCH.equals(userFlowType);
    }

    @Override
    public void perform(Update update) {
        perform(update, Map.of());
    }

    @Override
    public void perform(Update update, Map<String, Object> params) {
        Message message = update.getCallbackQuery().getMessage();
        if (!adminCommandService.isBotAdmin(message.getChat().getId())) {
            log.error("This user command can be handled for admin only");
            return;
        }

        ZonedDateTime currentZdt = ZonedDateTime.now();
        if (currentZdt.toLocalDate().getMonth().getValue() != Month.MARCH.getValue() &&
                currentZdt.getDayOfMonth() != 8) {
            log.warn("The command can be performed on 8 march only");
            messageSender.sendAnswerCallback(update.getCallbackQuery().getId(), "The command can be performed on 8 march only", true);
            return;
        }

        List<TgSource> sources = storageService.findPrivateSources(Gender.FEMALE);
        log.info("Found {} sources", sources.size());
        messageSender.deleteMessage(message.getChatId(), message.getMessageId());
        for (TgSource source : sources) {
            log.info("Sending congratulation for {} {} ({}, {})",
                    source.getFirstName(), source.getLastName(), source.getUsername(), source.getSourceId());
            String textToSend = congratulationText.replace("{name}", source.getFirstName());
            try {
            messageSender.sendSilently(textToSend, source.getSourceId(), null);
            } catch (Exception e) {
                log.error("unable to send message: {}", e.getMessage(), e);
            }
        }
    }
}
