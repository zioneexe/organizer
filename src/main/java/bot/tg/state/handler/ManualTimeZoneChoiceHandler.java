package bot.tg.state.handler;

import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimeZoneHelper;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

import static bot.tg.constant.TimeZone.Response.MANUAL_CHOICE_MESSAGE;

@Component
@RequiredArgsConstructor
public class ManualTimeZoneChoiceHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.TIMEZONE_MANUAL_CHOICE);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();

        InlineKeyboardMarkup timeZoneChoiceKeyboard = TimeZoneHelper.formTimeZoneChoiceKeyboard();
        TelegramHelper.sendMessageWithMarkup(telegramClient, userId, MANUAL_CHOICE_MESSAGE, timeZoneChoiceKeyboard);

        userStateManager.setState(userId, UserState.IDLE);
    }


}
