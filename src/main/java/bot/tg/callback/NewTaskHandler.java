package bot.tg.callback;

import bot.tg.service.TaskService;
import org.telegram.telegrambots.meta.api.objects.Update;

import static bot.tg.constant.Task.Callback.NEW_TASK;

public class NewTaskHandler implements CallbackHandler {

    private final TaskService taskService;

    public NewTaskHandler() {
        this.taskService = new TaskService();
    }

    @Override
    public boolean supports(String data) {
        return data.equals(NEW_TASK);
    }

    @Override
    public void handle(Update update) {
        this.taskService.startTaskCreation(update);
    }
}
