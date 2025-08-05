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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.PAGE_TASK;

@Component
@RequiredArgsConstructor
public class TaskPaginationHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PaginationService paginationService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(PAGE_TASK + COLON_DELIMITER);
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
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "❌ Некоректний запит на зміну сторінки.");
            return;
        }

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int neededPage = Integer.parseInt(parts[1]);
        Pageable pageable = paginationService.formTaskPageableForUser(neededPage, context.userId, LocalDate.now(), userZoneId);
        EditMessageText pageMessage = TasksResponseHelper.createTasksEditMessage(
                userSession,
                userRepository,
                taskRepository,
                pageable,
                LocalDate.now(),
                context
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
