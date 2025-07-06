package bot.tg.command;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class RemindersCommand implements BotCommand {

    private final TelegramClient telegramClient;
    private final ReminderRepository reminderRepository;

    public RemindersCommand() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
    }

    @Override
    public void execute(Update update) {
        SendMessage sendMessage = ReminderResponseHelper.createRemindersMessage(reminderRepository, update);
        TelegramHelper.safeExecute(telegramClient, sendMessage);
    }
}
