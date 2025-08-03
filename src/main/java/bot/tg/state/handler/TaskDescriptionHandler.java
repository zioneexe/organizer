package bot.tg.state.handler;

import bot.tg.dto.ChatContext;
import bot.tg.dto.create.TaskCreateDto;
import bot.tg.helper.TelegramHelper;
import bot.tg.mapper.TaskMapper;
import bot.tg.model.TodoTask;
import bot.tg.service.TaskService;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskDescriptionHandler implements StateHandler {

    private final UserStateManager userStateManager;
    private final TelegramClient telegramClient;
    private final TaskService taskService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_TASK_DESCRIPTION);
    }

    @Override
    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String text = update.getMessage().getText();

            if (text.length() > 512) {
                TelegramHelper.sendMessageWithForceReply(
                        telegramClient,
                        chatId,
                        "Опис занадто довгий. 🙈 Максимум 512 символів."
                );
                return;
            }

            userStateManager.setState(userId, UserState.IDLE);

            TaskCreateDto dto = userStateManager.getTaskDraft(userId);
            dto.setDescription(text);
            TodoTask task = TaskMapper.fromDto(dto);
            taskService.endTaskCreation(task, new ChatContext(userId, chatId));
        }
    }
}
