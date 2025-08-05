package bot.tg.handler.callback.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.DELETE_TASK;

@Component
@RequiredArgsConstructor
public class DeleteTaskHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PaginationService paginationService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "❌ Некоректний запит на видалення.");
            return;
        }

        String taskId = parts[1];
        boolean deleted = taskRepository.deleteById(taskId);

        String response = deleted
                ? "🗑 Завдання видалено."
                : "⚠️ Завдання не знайдено.";

        TelegramHelper.sendEditMessage(telegramClient, context.messageId, context.userId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userSession.getCurrentTaskPage();
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, context.userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                userSession,
                userRepository,
                taskRepository,
                pageable,
                context.userId,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);

        userSession.setIdleState();
    }
}
