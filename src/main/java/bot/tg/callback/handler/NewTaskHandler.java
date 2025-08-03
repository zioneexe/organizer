package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Task.Callback.NEW_TASK;

@Component
@RequiredArgsConstructor
public class NewTaskHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskService taskService;

    @Override
    public boolean supports(String data) {
        return data.equals(NEW_TASK);
    }

    @Override
    public void handle(Update update) {
        String callbackQueryId = update.getCallbackQuery().getId();

        this.taskService.startTaskCreation(update);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
