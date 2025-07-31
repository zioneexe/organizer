package bot.tg.callback;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
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

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.DELETE_TASK;

public class DeleteTaskHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public DeleteTaskHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.taskRepository = RepositoryProvider.getTaskRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, "❌ Некоректний запит на видалення.");
            return;
        }

        String taskId = parts[1];
        boolean deleted = taskRepository.deleteById(taskId);

        String response = deleted
                ? "🗑 Завдання видалено."
                : "⚠️ Завдання не знайдено.";

        TelegramHelper.sendEditMessage(telegramClient, messageId, chatId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);

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
                new ChatContext(userId, chatId),
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
