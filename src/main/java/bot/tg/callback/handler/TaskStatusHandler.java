package bot.tg.callback;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.Pageable;
import bot.tg.dto.update.TaskUpdateDto;
import bot.tg.model.TaskStatus;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserStateManager;
import bot.tg.util.PaginationHelper;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.COMPLETED_TASK;
import static bot.tg.constant.Task.Callback.IN_PROGRESS_TASK;
import static bot.tg.constant.Task.Response.TASK_COMPLETED;
import static bot.tg.constant.Task.Response.TASK_IN_PROGRESS;

public class TaskStatusHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public TaskStatusHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.taskRepository = RepositoryProvider.getTaskRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(IN_PROGRESS_TASK + COLON_DELIMITER) || data.startsWith(COMPLETED_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();
        String[] parts = data.split(COLON_DELIMITER);

        String status = parts[0];
        String taskId = parts[1];

        if (!taskRepository.existsById(taskId)) {
            return;
        }

        TaskStatus updatedStatus = TaskStatus.fromString(status);
        TaskUpdateDto dto = TaskUpdateDto.builder()
                .status(updatedStatus)
                .build();

        taskRepository.update(taskId, dto);

        String answerText = updatedStatus == TaskStatus.COMPLETED ? TASK_COMPLETED : TASK_IN_PROGRESS;
        TelegramHelper.sendCallbackAnswerWithMessageAlert(telegramClient, callbackQueryId, answerText);

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentTaskPage(userId);
        Pageable pageable = PaginationHelper.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
        EditMessageText editMessage = TasksResponseHelper.createTasksEditMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                LocalDate.now(),
                update);
        TelegramHelper.safeExecute(telegramClient, editMessage);
    }
}
