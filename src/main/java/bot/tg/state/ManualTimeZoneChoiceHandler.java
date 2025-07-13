package bot.tg.state;

import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimeZoneHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.TimeZone.Response.MANUAL_CHOICE_MESSAGE;

public class ManualTimeZoneChoiceHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    public ManualTimeZoneChoiceHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();

        InlineKeyboardMarkup timeZoneChoiceKeyboard = TimeZoneHelper.formTimeZoneChoiceKeyboard();
        TelegramHelper.sendMessageWithMarkup(telegramClient, userId, MANUAL_CHOICE_MESSAGE, timeZoneChoiceKeyboard);

        userStateManager.setState(userId, UserState.IDLE);
    }


}
