package bot.tg.callback;

import bot.tg.provider.TelegramClientProvider;
import bot.tg.service.TaskService;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Task.Callback.NEW_TASK;

public class NewTaskHandler implements CallbackHandler {

    private final TaskService taskService;

    public NewTaskHandler() {
        this.taskService = new TaskService();
    }

    @Override
    public boolean supports(String data) {
        return data.equals(NEW_TASK);
    }

    @Override
    public void handle(Update update) {
        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        String callbackQueryId = update.getCallbackQuery().getId();

        this.taskService.startTaskCreation(update);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
