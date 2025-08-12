package bot.tg.service;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;
    private final TimeZoneService timeZoneService;

    public void sendTasksFirstPage(UserRequest request) {
        TelegramContext context = request.getContext();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        Pageable pageable = paginationService.formTaskPageableForUser(Pageable.FIRST, context.userId, LocalDate.now(), userZoneId);
        SendMessage sendMessage = TasksResponseHelper.createTasksMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);
    }

    public void sendTasksCurrentPage(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        int currentPage = userSession.getCurrentTaskPage();
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, context.userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
    }

    public void sendTasksPageEdit(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        int currentPage = userSession.getCurrentTaskPage();
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, context.userId, LocalDate.now(), userZoneId);
        EditMessageText editMessage = TasksResponseHelper.createTasksEditMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, editMessage);
    }

    public void sendTasksPageEdit(UserRequest request, int neededPage) {
        TelegramContext context = request.getContext();

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = paginationService.formTaskPageableForUser(neededPage, context.userId, LocalDate.now(), userZoneId);
        EditMessageText pageMessage = TasksResponseHelper.createTasksEditMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
    }

    public void startTaskCreation(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, ALRIGHT);
        TelegramHelper.sendMessageWithForceReply(telegramClient, context.userId, TASK_TITLE);

        userSession.createTaskDraft();
        userSession.setState(UserState.AWAITING_TASK_TITLE);
    }

    public void endTaskCreation(UserRequest request, TodoTask task) {
        UserSession userSession = request.getUserSession();
        Long userId = userSession.getUserId();

        taskRepository.create(task);

        TelegramHelper.sendSimpleMessage(telegramClient, userId, TASK_CREATED);

        ZoneId userZoneId = timeZoneService.getUserZoneId(userId);

        int currentPage = userSession.getCurrentTaskPage();
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
    }
}
