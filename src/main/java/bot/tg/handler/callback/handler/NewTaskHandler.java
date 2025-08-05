package bot.tg.handler.callback.handler;

import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Task.Callback.NEW_TASK;

@Component
@RequiredArgsConstructor
public class NewTaskHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskService taskService;

    @Override
    public boolean supports(String data) {
        return data.equals(NEW_TASK);
    }

    @Override
    public void handle(UserRequest request) {
        String callbackQueryId = request.getUpdate().getCallbackQuery().getId();

        this.taskService.startTaskCreation(request);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
