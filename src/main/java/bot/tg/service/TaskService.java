package bot.tg.service;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.model.TodoTask;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.PaginationHelper;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.ResponseMessage.ALRIGHT;
import static bot.tg.constant.Task.Response.TASK_CREATED;
import static bot.tg.constant.Task.Response.TASK_TITLE;

public class TaskService {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.taskRepository = RepositoryProvider.getTaskRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    public void startTaskCreation(Update update) {
        ChatContext chatContext = TelegramHelper.extractChatContext(update);
        if (chatContext == null) return;
        long chatId = chatContext.getChatId();
        long userId = chatContext.getUserId();

        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, chatId, ALRIGHT);
        TelegramHelper.sendMessageWithForceReply(telegramClient, chatId, TASK_TITLE);

        userStateManager.createTaskDraft(userId);
        userStateManager.setState(userId, UserState.AWAITING_TASK_TITLE);
    }

    public void endTaskCreation(TodoTask task, ChatContext chatContext) {
        taskRepository.create(task);

        long chatId = chatContext.getChatId();
        long userId = chatContext.getUserId();

        TelegramHelper.sendSimpleMessage(telegramClient, chatId, TASK_CREATED);

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentTaskPage(userId);
        Pageable pageable = PaginationHelper.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                chatContext,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
    }
}
