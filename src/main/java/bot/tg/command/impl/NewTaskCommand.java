package bot.tg.command.impl;

import bot.tg.command.BotCommand;
import bot.tg.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class NewTaskCommand implements BotCommand {

    private final TaskService taskService;

    @Override
    public String getCommand() {
        return "/newtask";
    }

    @Override
    public void execute(Update update) {
        this.taskService.startTaskCreation(update);
    }
}
