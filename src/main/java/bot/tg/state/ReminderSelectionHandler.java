package bot.tg.state;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class ReminderSelectionHandler implements StateHandler {

    private final UserStateManager userStateManager;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final TelegramClient telegramClient;

    public ReminderSelectionHandler() {
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void handle(Update update) {
        SendMessage sendMessage = ReminderResponseHelper.createRemindersMessage(userRepository, reminderRepository, update);
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        long userId = update.getMessage().getFrom().getId();
        userStateManager.setState(userId, UserState.IDLE);
    }
}
