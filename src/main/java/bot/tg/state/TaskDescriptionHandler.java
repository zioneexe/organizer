package bot.tg.state;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.dto.create.TaskCreateDto;
import bot.tg.mapper.TaskMapper;
import bot.tg.model.TodoTask;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.util.PaginationHelper;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Task.Response.TASK_CREATED;

public class TaskDescriptionHandler implements StateHandler {

    private final UserStateManager userStateManager;
    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public TaskDescriptionHandler() {
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.taskRepository = RepositoryProvider.getTaskRepository();
    }

    @Override
    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String text = update.getMessage().getText();

            if (text.length() > 512) {
                TelegramHelper.sendMessageWithForceReply(
                        telegramClient,
                        chatId,
                        "Опис занадто довгий. 🙈 Максимум 512 символів."
                );
                return;
            }

            userStateManager.setState(userId, UserState.IDLE);

            TaskCreateDto dto = userStateManager.getTaskDraft(userId);
            dto.setDescription(text);

            TodoTask task = TaskMapper.fromDto(dto);
            taskRepository.create(task);
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, TASK_CREATED);

            String userTimeZone = userRepository.getById(userId).getTimeZone();
            ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                    ZoneId.systemDefault() :
                    ZoneId.of(userTimeZone);

            int currentPage = userStateManager.getCurrentTaskPage(userId);
            Pageable pageable = PaginationHelper.formPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
            SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                    userStateManager,
                    userRepository,
                    taskRepository,
                    pageable,
                    new ChatContext(userId, chatId),
                    LocalDate.now()
            );
            TelegramHelper.safeExecute(telegramClient, tasksMessage);
        }
    }
}
