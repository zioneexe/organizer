package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.model.Reminder;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.state.UserStateManager;
import bot.tg.util.PaginationHelper;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Callback.DISABLE_REMINDER;
import static bot.tg.constant.Reminder.Callback.ENABLE_REMINDER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class ReminderToggleHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public ReminderToggleHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.messageService = ServiceProvider.getMessageService();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DISABLE_REMINDER + COLON_DELIMITER)
                || data.startsWith(ENABLE_REMINDER + COLON_DELIMITER);
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
        Reminder reminder = reminderRepository.getById(reminderId);

        String response = "";
        if (data.startsWith(DISABLE_REMINDER + COLON_DELIMITER)) {
            reminderRepository.setEnabled(reminderId, false);
            messageService.cancelReminder(reminder);
            response = "\uD83D\uDD15 Нагадування вимкнено.";
        } else if (data.startsWith(ENABLE_REMINDER + COLON_DELIMITER)) {
            reminderRepository.setEnabled(reminderId, true);
            messageService.scheduleReminder(reminder);
            response = "\uD83D\uDD14 Нагадування увімкнено.";
        }

        TelegramHelper.sendEditMessage(telegramClient, messageId, chatId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentReminderPage(userId);
        Pageable pageable = PaginationHelper.formReminderPageableForUser(currentPage, userId, userZoneId);
        SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(
                userStateManager,
                userRepository,
                reminderRepository,
                pageable,
                new ChatContext(userId, chatId)
        );
        TelegramHelper.safeExecute(telegramClient, remindersMessage);
    }
}
