package bot.tg.command;

import bot.tg.service.ReminderService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class NewReminderCommand implements BotCommand {

    private final ReminderService reminderService;

    public NewReminderCommand() {
        this.reminderService = new ReminderService();
    }

    @Override
    public void execute(Update update) {
        this.reminderService.startReminderCreation(update);
    }

}
