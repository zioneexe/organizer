package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.update.TaskUpdateDto;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.validation.Violation;
import bot.tg.util.validation.impl.TaskAndReminderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Set;

import static bot.tg.constant.Task.Response.*;

@Component
@RequiredArgsConstructor
public class TaskEditingHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;
    private final TaskAndReminderValidator validator;
    private final TaskService taskService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.EDITING_TASK_NAME, UserState.EDITING_TASK_DESCRIPTION);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        Update update = request.getUpdate();
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        UserSession userSession = request.getUserSession();

        UserState state = userSession.getState();
        String taskId = userSession.getEditingTaskId();
        if (taskId == null) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, TASK_UPDATE_NOT_FOUND);
            userSession.setState(UserState.IDLE);
            return;
        }

        switch (state) {
            case EDITING_TASK_NAME -> {
                List<Violation> violations = validator.validateTitle(context.text);
                if (!violations.isEmpty()) {
                    TelegramHelper.sendMessageWithForceReply(
                            telegramClient,
                            context.userId,
                            violations.getFirst().getMessage()
                    );
                    return;
                }
                TaskUpdateDto dto = TaskUpdateDto.builder().title(context.text).build();
                taskRepository.update(taskId, dto);
                TelegramHelper.sendSimpleMessage(telegramClient, context.userId, TASK_UPDATE_LABEL_SUCCESS);
            }

            case EDITING_TASK_DESCRIPTION -> {
                List<Violation> violations = validator.validateDescription(context.text);
                if (!violations.isEmpty()) {
                    TelegramHelper.sendMessageWithForceReply(
                            telegramClient,
                            context.userId,
                            violations.getFirst().getMessage()
                    );
                    return;
                }

                TaskUpdateDto dto = TaskUpdateDto.builder().description(context.text).build();
                taskRepository.update(taskId, dto);
                TelegramHelper.sendSimpleMessage(telegramClient, context.chatId, TASK_UPDATE_DESCRIPTION_SUCCESS);
            }
        }

        userSession.setState(UserState.IDLE);
        userSession.clearEditingTaskId();

        taskService.sendTasksCurrentPage(request);
    }
}
