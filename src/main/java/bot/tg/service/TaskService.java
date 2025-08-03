package bot.tg.service;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
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
