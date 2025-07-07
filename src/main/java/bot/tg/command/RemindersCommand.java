package bot.tg.command;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class RemindersCommand implements BotCommand {

    private final TelegramClient telegramClient;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final UserStateManager userStateManager;

    public RemindersCommand() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    @Override
    public void execute(Update update) {
        SendMessage sendMessage = ReminderResponseHelper.createRemindersMessage(userRepository, reminderRepository, update);
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        long userId = update.getMessage().getFrom().getId();
        userStateManager.setState(userId, UserState.IDLE);
    }
}
