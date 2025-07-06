package bot.tg.callback;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.util.Constants.COLON_DELIMITER;
import static bot.tg.util.Constants.DELETE_REMINDER;

public class DeleteReminderHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final ReminderRepository reminderRepository;

    public DeleteReminderHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_REMINDER + COLON_DELIMITER);
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

        String reminderId = parts[1];
        boolean deleted = reminderRepository.deleteById(reminderId);

        String response = deleted
                ? "🗑 Нагадування видалено."
                : "⚠️ Нагадування не знайдено або не ваше.";

        TelegramHelper.sendSimpleMessage(telegramClient, chatId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
