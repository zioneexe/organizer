package bot.tg.handler.command.impl;

import bot.tg.handler.command.BotCommand;
import bot.tg.service.ReminderService;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemindersCommand extends BotCommand {

    private final ReminderService reminderService;

    @Override
    public String getCommand() {
        return "/reminders";
    }

    @Override
    public void handle(UserRequest request) {
        reminderService.sendRemindersFirstPage(request);
        request.getUserSession().setIdleState();
    }
}
