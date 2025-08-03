package bot.tg.command.impl;

import bot.tg.command.BotCommand;
import bot.tg.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class NewReminderCommand implements BotCommand {

    private final ReminderService reminderService;

    @Override
    public String getCommand() {
        return "/newreminder";
    }

    @Override
    public void execute(Update update) {
        this.reminderService.startReminderCreation(update);
    }

}
