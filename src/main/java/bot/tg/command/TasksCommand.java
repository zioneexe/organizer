package bot.tg.command;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.util.ResponseMessageHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TasksCommand implements BotCommand {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    public TasksCommand() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getInstance().getTaskRepository();
    }

    @Override
    public void execute(Update update) {
        SendMessage sendMessage = ResponseMessageHelper.createTasksMessage(taskRepository, update);
        TelegramHelper.safeExecute(telegramClient, sendMessage);
    }
}
