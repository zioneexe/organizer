package bot.tg.handler.state.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskSelectionHandler extends StateHandler {

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
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = paginationService.formTaskPageableForUser(Pageable.FIRST, context.userId, LocalDate.now(), userZoneId);
        SendMessage sendMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                context.userId,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        userStateManager.setState(context.userId, UserState.IDLE);
    }
}
