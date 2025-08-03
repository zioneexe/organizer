package bot.tg.state.handler;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.dto.update.TaskUpdateDto;
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
public class TaskEditingHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.EDITING_TASK_NAME, UserState.EDITING_TASK_DESCRIPTION);
    }

    @Override
    public void handle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        UserState state = userStateManager.getState(userId);

        String taskId = userStateManager.getEditingTaskId(userId);
        if (taskId == null) {
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, "Помилка: завдання для редагування не знайдено.");
            userStateManager.setState(userId, UserState.IDLE);
            return;
        }

        switch (state) {
            case EDITING_TASK_NAME -> {
                if (text.length() > 40) {
                    TelegramHelper.sendMessageWithForceReply(
                            telegramClient,
                            chatId,
                            "Назва занадто довга. 🙈 Скороти до 40 символів."
                    );
                    return;
                }
                TaskUpdateDto dto = TaskUpdateDto.builder().title(text).build();
                taskRepository.update(taskId, dto);
                TelegramHelper.sendSimpleMessage(telegramClient, chatId, "Назву оновлено ✅");
            }

            case EDITING_TASK_DESCRIPTION -> {
                if (text.length() > 512) {
                    TelegramHelper.sendMessageWithForceReply(
                            telegramClient,
                            chatId,
                            "Опис занадто довгий. 🙈 Максимум 512 символів."
                    );
                    return;
                }

                TaskUpdateDto dto = TaskUpdateDto.builder().description(text).build();
                taskRepository.update(taskId, dto);
                TelegramHelper.sendSimpleMessage(telegramClient, chatId, "Опис оновлено ✅");
            }
        }

        userStateManager.setState(userId, UserState.IDLE);
        userStateManager.clearEditingTaskId(userId);

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentTaskPage(userId);
        Pageable pageable = paginationService.formTaskPageableForUser(currentPage, userId, LocalDate.now(), userZoneId);
        SendMessage tasksMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                new ChatContext(userId, chatId),
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, tasksMessage);
    }
}
