package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.ResponseMessage.INCORRECT_REQUEST_PAGE;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.PAGE_TASK;

@Component
@RequiredArgsConstructor
public class TaskPaginationHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskService taskService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(PAGE_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, INCORRECT_REQUEST_PAGE);
            return;
        }

        int neededPage = Integer.parseInt(parts[1]);
        taskService.sendTasksPageEdit(request, neededPage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
