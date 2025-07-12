package bot.tg.command;

import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.MenuHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand implements BotCommand {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    public StartCommand() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    @Override
    public void execute(Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage message = MenuHelper.formMenuMessage(chatId);
        TelegramHelper.safeExecute(telegramClient, message);

        long userId = update.getMessage().getFrom().getId();
        userStateManager.setState(userId, UserState.IDLE);
    }
}
