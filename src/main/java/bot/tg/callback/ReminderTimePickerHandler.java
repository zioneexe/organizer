package bot.tg.callback;

import bot.tg.dto.DateTime;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimePickerResponseHelper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static bot.tg.constant.Callback.CANCEL;
import static bot.tg.constant.Callback.CONFIRM;
import static bot.tg.constant.Reminder.Callback.*;
import static bot.tg.constant.Reminder.Response.REMINDER_TEXT;
import static bot.tg.constant.ResponseMessage.INVALID_TIME;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class ReminderTimePickerHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    public ReminderTimePickerHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(TIME_PICKER + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) return;

        String action = parts[1];
        ReminderCreateDto dto = ServiceProvider.getUserStateManager().getReminderDraft(userId);
        DateTime dateTime = dto.getDateTime();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId zoneId = userTimeZone != null && !userTimeZone.isBlank() ?
                ZoneId.of(userTimeZone) :
                ZoneId.systemDefault();

        boolean isToday = dateTime.getDate().isEqual(LocalDate.now(zoneId));
        int currentHour = LocalTime.now(zoneId).getHour();
        int currentMinute = LocalTime.now(zoneId).getMinute();

        boolean isValidAction = true;
        switch (action) {
            case CHANGE_HOUR -> {
                int delta = Integer.parseInt(parts[2]);
                int newHour = (dateTime.getHour() + delta + 24) % 24;

                if (isToday && newHour < currentHour) isValidAction = false;
                if (isValidAction) {
                    dateTime.setHour(newHour);
                    dateTime.setTimeManuallyEdited(true);
                }
            }
            case CHANGE_MINUTE -> {
                int previousHour = dateTime.getHour();
                int delta = Integer.parseInt(parts[2]);
                int newMinute = (dateTime.getMinute() + delta + 60) % 60;

                if (isToday && previousHour < currentHour) isValidAction = false;
                if (isToday && previousHour == currentHour && newMinute < currentMinute) isValidAction = false;

                if (isValidAction) {
                    dateTime.setMinute(newMinute);
                    dateTime.setTimeManuallyEdited(true);
                }
            }
            case CONFIRM -> {
                ZonedDateTime now = ZonedDateTime.now(zoneId).withSecond(0).withNano(0);
                ZonedDateTime currentlySetTime = DateTime.DateTimeMapper.toZonedDateTime(dateTime).withSecond(0).withNano(0);

                if (now.isAfter(currentlySetTime)) {
                    isValidAction = false;
                    break;
                }

                ServiceProvider.getUserStateManager().setState(userId, UserState.AWAITING_REMINDER_TEXT);
                TelegramHelper.sendMessageWithForceReply(telegramClient, chatId, REMINDER_TEXT);
            }
            case CANCEL -> {
                TelegramHelper.sendEditMessage(telegramClient, messageId, chatId, "Створення скасовано.");
                return;
            }
        }

        if (!isValidAction) {
            TelegramHelper.sendCallbackAnswerWithMessage(telegramClient, callbackQueryId, INVALID_TIME);
        }

        EditMessageText editMessage = TimePickerResponseHelper.createTimePickerEditMessage(update, userTimeZone);
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
