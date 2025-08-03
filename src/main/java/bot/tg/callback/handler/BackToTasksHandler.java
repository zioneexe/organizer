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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

import static bot.tg.constant.Task.Callback.BACK_TO_TASKS;

@Component
@RequiredArgsConstructor
public class BackToTasksHandler implements CallbackHandler {

    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TelegramClient telegramClient;
    private final PaginationService paginationService;

    @Override
    public boolean supports(String data) {
        return data.equals(BACK_TO_TASKS);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentTaskPage(userId);
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
        EditMessageText editMessage = TasksResponseHelper.createTasksEditMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                LocalDate.now(),
                update
        );
        TelegramHelper.safeExecute(telegramClient, editMessage);
    }
}
