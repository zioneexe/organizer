package bot.tg.handler.state.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.dto.update.TaskUpdateDto;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import bot.tg.validation.TaskAndReminderValidator;
import bot.tg.validation.Violation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskEditingHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;
    private final TaskAndReminderValidator validator;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.EDITING_TASK_NAME, UserState.EDITING_TASK_DESCRIPTION);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        Update update = request.getUpdate();
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        UserState state = userStateManager.getState(context.userId);
        String taskId = userStateManager.getEditingTaskId(context.userId);
        if (taskId == null) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "Помилка: завдання для редагування не знайдено.");
            userStateManager.setState(context.userId, UserState.IDLE);
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
                TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "Назву оновлено ✅");
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
                TelegramHelper.sendSimpleMessage(telegramClient, context.chatId, "Опис оновлено ✅");
            }
        }

        userStateManager.setState(context.userId, UserState.IDLE);
        userStateManager.clearEditingTaskId(context.userId);

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentTaskPage(context.userId);
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, context.userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                context.userId,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
    }
}
