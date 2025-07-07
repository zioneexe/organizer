package bot.tg.command;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TasksCommand implements BotCommand {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;
    private final UserStateManager userStateManager;

    public TasksCommand() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getTaskRepository();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    @Override
    public void execute(Update update) {
        SendMessage sendMessage = TasksResponseHelper.createTasksMessage(taskRepository, update);
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        long userId = update.getMessage().getFrom().getId();
        userStateManager.setState(userId, UserState.IDLE);
    }
}
