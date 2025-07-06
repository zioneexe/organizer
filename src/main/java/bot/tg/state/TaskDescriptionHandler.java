package bot.tg.state;

import bot.tg.dto.create.TaskCreateDto;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.util.ResponseMessageHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.util.Constants.TASK_CREATED;

public class TaskDescriptionHandler implements StateHandler {

    private final UserStateManager userStateManager;
    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    public TaskDescriptionHandler() {
        this.userStateManager = ServiceProvider.getInstance().getUserStateManager();
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getInstance().getTaskRepository();
    }

    @Override
    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String text = update.getMessage().getText();

            userStateManager.setState(userId, UserState.IDLE);

            TaskCreateDto dto = userStateManager.getDraft(userId);
            dto.setDescription(text);

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(TASK_CREATED)
                    .build();

            taskRepository.create(dto);
            TelegramHelper.safeExecute(telegramClient, sendMessage);

            SendMessage tasksMessage = ResponseMessageHelper.createTasksMessage(taskRepository, update);
            TelegramHelper.safeExecute(telegramClient, tasksMessage);
        }
    }
}
