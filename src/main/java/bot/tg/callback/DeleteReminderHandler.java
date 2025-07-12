package bot.tg.callback;

import bot.tg.dto.ChatContext;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Callback.DELETE_REMINDER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class DeleteReminderHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public DeleteReminderHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_REMINDER + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, "❌ Некоректний запит на видалення.");
            return;
        }

        String reminderId = parts[1];
        boolean deleted = reminderRepository.deleteById(reminderId);

        String response = deleted
                ? "🗑 Нагадування видалено."
                : "⚠️ Нагадування не знайдено.";

        TelegramHelper.sendEditMessage(telegramClient, messageId, chatId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);

        SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(userRepository, reminderRepository, new ChatContext(userId, chatId));
        TelegramHelper.safeExecute(telegramClient, remindersMessage);
    }
}
