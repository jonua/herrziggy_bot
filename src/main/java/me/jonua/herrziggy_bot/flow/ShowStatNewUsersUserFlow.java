package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.data.jpa.projections.TgSourceProjection;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShowStatNewUsersUserFlow extends BaseStatUsersUserFlow {
    private final StorageService storageService;

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.SHOW_STAT_NEW_USERS.equals(userFlowType);
    }

    @Override
    public void perform(Update update) {
        perform(update, Map.of());
    }

    @Override
    public void perform(Update update, Map<String, Object> params) {
        Map<Date, List<TgSourceProjection>> stat = storageService.getStatNewUsers();
        respondWithStat(update.getCallbackQuery().getId(), stat);
    }
}
