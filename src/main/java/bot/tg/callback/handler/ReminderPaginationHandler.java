package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.Pageable;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserStateManager;
import bot.tg.util.PaginationHelper;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Callback.PAGE_REMINDER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class ReminderPaginationHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    public ReminderPaginationHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(PAGE_REMINDER + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        long chatId = callback.getMessage().getChatId();
        long userId = callback.getFrom().getId();
        String data = callback.getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, "❌ Некоректний запит на зміну сторінки.");
            return;
        }

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int neededPage = Integer.parseInt(parts[1]);
        Pageable pageable = PaginationHelper.formReminderPageableForUser(neededPage, userId, userZoneId);
        EditMessageText pageMessage = ReminderResponseHelper.createRemindersEditMessage(
                userStateManager,
                userRepository,
                reminderRepository,
                pageable,
                update
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callback.getId());
    }
}
