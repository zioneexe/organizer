package bot.tg.handler.callback.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.dto.update.TaskUpdateDto;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.TaskStatus;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.service.TimeZoneService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.COMPLETED_TASK;
import static bot.tg.constant.Task.Callback.IN_PROGRESS_TASK;
import static bot.tg.constant.Task.Response.TASK_COMPLETED;
import static bot.tg.constant.Task.Response.TASK_IN_PROGRESS;

@Component
@RequiredArgsConstructor
public class TaskStatusHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PaginationService paginationService;
    private final TimeZoneService timeZoneService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(IN_PROGRESS_TASK + COLON_DELIMITER) || data.startsWith(COMPLETED_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);

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
        TelegramHelper.sendCallbackAnswerWithMessageAlert(telegramClient, context.callbackQueryId, answerText);

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);

        int currentPage = userSession.getCurrentTaskPage();
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, context.userId, LocalDate.now(), userZoneId);
        EditMessageText editMessage = TasksResponseHelper.createTasksEditMessage(
                userSession,
                userRepository,
                taskRepository,
                pageable,
                LocalDate.now(),
                context);
        TelegramHelper.safeExecute(telegramClient, editMessage);
    }
}
