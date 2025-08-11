package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.TaskCreateDto;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.mapper.TaskMapper;
import bot.tg.model.TodoTask;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Task.Callback.SKIP_DESCRIPTION_TASK;
import static bot.tg.constant.Task.Response.TASK_NO_DESCRIPTION_RESPONSE;

@Component
@RequiredArgsConstructor
public class SkipDescriptionHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskService taskService;

    @Override
    public boolean supports(String data) {
        return data.equals(SKIP_DESCRIPTION_TASK);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        TelegramHelper.sendSimpleMessage(telegramClient, context.userId, TASK_NO_DESCRIPTION_RESPONSE);

        TaskCreateDto dto = userSession.getTaskDraft();
        dto.setDescription(null);
        TodoTask task = TaskMapper.fromDto(dto);
        taskService.endTaskCreation(request, task);

        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
