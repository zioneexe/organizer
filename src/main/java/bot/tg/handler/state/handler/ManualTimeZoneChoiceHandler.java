package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimeZoneHelper;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

import static bot.tg.constant.TimeZone.Response.MANUAL_CHOICE_MESSAGE;

@Component
@RequiredArgsConstructor
public class ManualTimeZoneChoiceHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.TIMEZONE_MANUAL_CHOICE);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        InlineKeyboardMarkup timeZoneChoiceKeyboard = TimeZoneHelper.formTimeZoneChoiceKeyboard();
        TelegramHelper.sendMessageWithMarkup(telegramClient, context.userId, MANUAL_CHOICE_MESSAGE, timeZoneChoiceKeyboard);

        userStateManager.setState(context.userId, UserState.IDLE);
    }
}
