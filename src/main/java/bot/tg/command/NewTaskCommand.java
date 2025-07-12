package bot.tg.command;

import bot.tg.service.TaskService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class NewTaskCommand implements BotCommand {

    private final TaskService taskService;

    public NewTaskCommand() {
        this.taskService = new TaskService();
    }

    @Override
    public void execute(Update update) {
        this.taskService.startTaskCreation(update);
    }
}
