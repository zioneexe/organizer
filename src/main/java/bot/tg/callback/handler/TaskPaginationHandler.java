package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.Pageable;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.PAGE_TASK;

@Component
@RequiredArgsConstructor
public class TaskPaginationHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PaginationService paginationService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(PAGE_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        long chatId = callback.getMessage().getChatId();
        long userId = callback.getFrom().getId();
        String data = callback.getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, "❌ Некоректний запит на зміну сторінки.");
            return;
        }

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int neededPage = Integer.parseInt(parts[1]);
        Pageable pageable = paginationService.formTaskPageableForUser(neededPage, userId, LocalDate.now(), userZoneId);
        EditMessageText pageMessage = TasksResponseHelper.createTasksEditMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                LocalDate.now(),
                update
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callback.getId());
    }
}
