package bot.tg.callback;

import bot.tg.service.ReminderService;
import org.telegram.telegrambots.meta.api.objects.Update;

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
        this.reminderService.startReminderCreation(update);
    }

}
