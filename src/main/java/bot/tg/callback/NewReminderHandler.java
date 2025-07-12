package bot.tg.callback;

import bot.tg.provider.TelegramClientProvider;
import bot.tg.service.ReminderService;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Callback.NEW_REMINDER;

public class NewReminderHandler implements CallbackHandler {

    private final ReminderService reminderService;

    public NewReminderHandler() {
        this.reminderService = new ReminderService();
    }

    @Override
    public boolean supports(String data) {
        return data.equals(NEW_REMINDER);
    }

    @Override
    public void handle(Update update) {
        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        String callbackQueryId = update.getCallbackQuery().getId();

        this.reminderService.startReminderCreation(update);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }

}
