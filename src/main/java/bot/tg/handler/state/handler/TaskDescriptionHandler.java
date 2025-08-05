package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.TaskCreateDto;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.mapper.TaskMapper;
import bot.tg.model.TodoTask;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.validation.TaskAndReminderValidator;
import bot.tg.util.validation.Violation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskDescriptionHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final TaskService taskService;
    private final TaskAndReminderValidator validator;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_TASK_DESCRIPTION);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        List<Violation> violations = validator.validateDescription(context.text);
        if (!violations.isEmpty()) {
            TelegramHelper.sendMessageWithForceReply(
                    telegramClient,
                    context.userId,
                    violations.getFirst().getMessage()
            );
            return;
        }

        userSession.setIdleState();

        TaskCreateDto dto = userSession.getTaskDraft();
        dto.setDescription(context.text);
        TodoTask task = TaskMapper.fromDto(dto);
        taskService.endTaskCreation(request, task);
    }
}
