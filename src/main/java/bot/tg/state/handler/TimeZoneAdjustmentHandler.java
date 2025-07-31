package bot.tg.state.handler;

import bot.tg.dto.ChatContext;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimeZoneHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TimeZoneAdjustmentHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    public TimeZoneAdjustmentHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
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
