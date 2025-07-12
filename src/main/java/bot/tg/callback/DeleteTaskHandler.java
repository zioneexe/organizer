package bot.tg.callback;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.DELETE_TASK;

public class DeleteTaskHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    public DeleteTaskHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getTaskRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(COLON_DELIMITER, 2);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, "❌ Некоректний запит на видалення.");
            return;
        }

        String taskId = parts[1];
        boolean deleted = taskRepository.deleteById(taskId);

        String response = deleted
                ? "🗑 Завдання видалено."
                : "⚠️ Завдання не знайдено.";

        TelegramHelper.sendSimpleMessage(telegramClient, chatId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
