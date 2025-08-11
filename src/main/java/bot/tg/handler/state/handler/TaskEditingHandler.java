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
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.validation.Violation;
import bot.tg.util.validation.impl.TaskAndReminderValidator;
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

        UserSession userSession = request.getUserSession();

        UserState state = userSession.getState();
        String taskId = userSession.getEditingTaskId();
        if (taskId == null) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "Помилка: завдання для редагування не знайдено.");
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

        userSession.setState(UserState.IDLE);
        userSession.clearEditingTaskId();

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
    }
}
