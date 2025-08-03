package bot.tg.service;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.ResponseMessage.ALRIGHT;
import static bot.tg.constant.Task.Response.TASK_CREATED;
import static bot.tg.constant.Task.Response.TASK_TITLE;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;

    public void startTaskCreation(TelegramContext context) {
        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, ALRIGHT);
        TelegramHelper.sendMessageWithForceReply(telegramClient, context.userId, TASK_TITLE);

        userStateManager.createTaskDraft(context.userId);
        userStateManager.setState(context.userId, UserState.AWAITING_TASK_TITLE);
    }

    public void endTaskCreation(TodoTask task, Long userId) {
        taskRepository.create(task);

        TelegramHelper.sendSimpleMessage(telegramClient, userId, TASK_CREATED);

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentTaskPage(userId);
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                userId,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
    }
}
