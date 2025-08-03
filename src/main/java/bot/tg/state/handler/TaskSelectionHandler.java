package bot.tg.state.handler;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskSelectionHandler implements StateHandler {

    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TelegramClient telegramClient;
    private final PaginationService paginationService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_TASK_SELECTION);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = paginationService.formTaskPageableForUser(Pageable.FIRST, userId, LocalDate.now(), userZoneId);
        SendMessage sendMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                new ChatContext(userId, chatId),
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
