package bot.tg.state.handler;

import bot.tg.dto.ChatContext;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimeZoneHelper;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TimeZoneAdjustmentHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.ADJUSTING_TIMEZONE);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChat().getId();

        SendMessage askMessage = TimeZoneHelper.formAdjustTimeZoneMessage(new ChatContext(userId, chatId));
        TelegramHelper.safeExecute(telegramClient, askMessage);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
