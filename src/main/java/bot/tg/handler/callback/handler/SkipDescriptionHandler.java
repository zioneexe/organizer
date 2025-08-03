package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.TaskCreateDto;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.mapper.TaskMapper;
import bot.tg.model.TodoTask;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Task.Callback.SKIP_DESCRIPTION_TASK;

@Component
@RequiredArgsConstructor
public class SkipDescriptionHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final TaskService taskService;

    @Override
    public boolean supports(String data) {
        return data.equals(SKIP_DESCRIPTION_TASK);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "Окей, без опису 👍");

        TaskCreateDto dto = userStateManager.getTaskDraft(context.userId);
        dto.setDescription(null);
        TodoTask task = TaskMapper.fromDto(dto);
        taskService.endTaskCreation(task, context.userId);

        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
