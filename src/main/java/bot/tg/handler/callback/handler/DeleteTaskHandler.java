package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.ResponseMessage.INCORRECT_REQUEST_DELETE;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.DELETE_TASK;
import static bot.tg.constant.Task.Response.TASK_DELETED;
import static bot.tg.constant.Task.Response.TASK_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class DeleteTaskHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, INCORRECT_REQUEST_DELETE);
            return;
        }

        String taskId = parts[1];
        boolean deleted = taskRepository.deleteById(taskId);

        String response = deleted ? TASK_DELETED : TASK_NOT_FOUND;

        TelegramHelper.sendEditMessage(telegramClient, context.messageId, context.userId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);

        taskService.sendTasksCurrentPage(request);

        userSession.setIdleState();
    }
}
