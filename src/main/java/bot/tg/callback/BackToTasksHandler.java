package bot.tg.callback;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.util.Constants.BACK_TO_TASKS;

public class BackToTasksHandler implements CallbackHandler {

    private final TaskRepository taskRepository;
    private final TelegramClient telegramClient;

    public BackToTasksHandler() {
        this.taskRepository = RepositoryProvider.getTaskRepository();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public boolean supports(String data) {
        return data.equals(BACK_TO_TASKS);
    }

    @Override
    public void handle(Update update) {
        EditMessageText editMessage = TasksResponseHelper.createTasksEditMessage(taskRepository, update);
        TelegramHelper.safeExecute(telegramClient, editMessage);
    }
}
