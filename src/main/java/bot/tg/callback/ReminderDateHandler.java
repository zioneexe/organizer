package bot.tg.callback;

import bot.tg.dto.DateTimeDto;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimePickerResponseHelper;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static bot.tg.util.Constants.COLON_DELIMITER;
import static bot.tg.util.Constants.DATE_REMINDER;

public class ReminderDateHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    public ReminderDateHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DATE_REMINDER);
    }

    @Override
    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        long userId = update.getCallbackQuery().getFrom().getId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String dateString = data.split(COLON_DELIMITER)[1];
        LocalDate date = parseDate(dateString);
        ReminderCreateDto dto = userStateManager.getReminderDraft(userId);
        DateTimeDto dateTimeDto = dto.getDateTime();

        LocalDateTime now = LocalDateTime.now();
        dateTimeDto.setDate(date);
        dateTimeDto.setHour(now.getHour());
        dateTimeDto.setMinute(now.getMinute());

        userStateManager.setState(userId, UserState.AWAITING_REMINDER_TIME);

        EditMessageText editMessage = TimePickerResponseHelper.createTimePickerEditMessage(update);
        TelegramHelper.safeExecute(telegramClient, editMessage);

        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        TelegramHelper.safeExecute(telegramClient, answer);
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
