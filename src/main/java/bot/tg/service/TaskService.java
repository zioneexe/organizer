package bot.tg.service;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.helper.TaskHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Core.Response.ALRIGHT;
import static bot.tg.constant.Task.Response.TASK_CREATED;
import static bot.tg.constant.Task.Response.TASK_TITLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;
    private final PaginationService paginationService;
    private final TimeZoneService timeZoneService;

    public void sendTasksFirstPage(UserRequest request) {
        TelegramContext context = request.getContext();
        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        log.debug("Sending first tasks page for userId={}", context.userId);

        Pageable pageable = paginationService.formTaskPageableForUser(Pageable.FIRST, context.userId, LocalDate.now(), userZoneId);
        SendMessage sendMessage = TaskHelper.createTasksMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);
        log.debug("First tasks page sent for userId={}", context.userId);
    }

    public void sendTasksCurrentPage(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        int currentPage = userSession.getCurrentTaskPage();
        log.debug("Sending current tasks page {} for userId={}", currentPage, context.userId);

        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, context.userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TaskHelper.createTasksMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
        log.debug("Current tasks page {} sent for userId={}", currentPage, context.userId);
    }

    public void sendTasksPageEdit(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        int currentPage = userSession.getCurrentTaskPage();
        log.debug("Editing current tasks page {} for userId={}", currentPage, context.userId);

        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, context.userId, LocalDate.now(), userZoneId);
        EditMessageText editMessage = TaskHelper.createTasksEditMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, editMessage);
        log.debug("Current tasks page {} edited for userId={}", currentPage, context.userId);
    }

    public void sendTasksPageEdit(UserRequest request, int neededPage) {
        TelegramContext context = request.getContext();
        log.debug("Editing tasks page {} for userId={}", neededPage, context.userId);

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        Pageable pageable = paginationService.formTaskPageableForUser(neededPage, context.userId, LocalDate.now(), userZoneId);
        EditMessageText pageMessage = TaskHelper.createTasksEditMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
        log.debug("Tasks page {} edited for userId={}", neededPage, context.userId);
    }

    public void startTaskCreation(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();
        log.debug("Starting task creation for userId={}", context.userId);

        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, ALRIGHT);
        TelegramHelper.sendMessageWithForceReply(telegramClient, context.userId, TASK_TITLE);

        userSession.createTaskDraft();
        userSession.setState(UserState.AWAITING_TASK_TITLE);
        log.debug("Task draft created and state set to AWAITING_TASK_TITLE for userId={}", context.userId);
    }

    public void endTaskCreation(UserRequest request, TodoTask task) {
        UserSession userSession = request.getUserSession();
        Long userId = userSession.getUserId();
        log.debug("Ending task creation for userId={}", userId);

        taskRepository.create(task);
        TelegramHelper.sendSimpleMessage(telegramClient, userId, TASK_CREATED);

        ZoneId userZoneId = timeZoneService.getUserZoneId(userId);
        int currentPage = userSession.getCurrentTaskPage();
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TaskHelper.createTasksMessage(
                request,
                timeZoneService,
                taskRepository,
                pageable,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
        log.debug("Task created and tasks page {} sent for userId={}", currentPage, userId);
    }
}
