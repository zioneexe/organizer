package bot.tg.state;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TaskSelectionHandler implements StateHandler {

    private final TaskRepository taskRepository;
    private final TelegramClient telegramClient;

    public TaskSelectionHandler() {
        this.taskRepository = RepositoryProvider.getTaskRepository();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void handle(Update update) {
        SendMessage sendMessage = TasksResponseHelper.createTasksMessage(taskRepository, update);
        TelegramHelper.safeExecute(telegramClient, sendMessage);
    }
}
