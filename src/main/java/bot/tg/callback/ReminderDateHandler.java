package bot.tg.callback;

import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimePickerResponseHelper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static bot.tg.constant.Reminder.Callback.DATE_PICKER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class ReminderDateHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;

    public ReminderDateHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DATE_PICKER + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        ReminderCreateDto dto = userStateManager.getReminderDraft(userId);
        String dateString = data.split(COLON_DELIMITER)[1];
        LocalDate date = parseDate(dateString);
        dto.getDateTime().setDate(date);
        userStateManager.setState(userId, UserState.AWAITING_REMINDER_TIME);

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        EditMessageText editMessage = TimePickerResponseHelper.createTimePickerEditMessage(update, userTimeZone);

        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
