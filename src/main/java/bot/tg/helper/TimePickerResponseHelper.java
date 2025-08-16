package bot.tg.helper;

import bot.tg.dto.DateTime;
import bot.tg.dto.TelegramContext;
import bot.tg.dto.Time;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserSession;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static bot.tg.constant.Callback.IGNORE;
import static bot.tg.constant.Core.Pagination.*;
import static bot.tg.constant.Core.Response.CANCEL;
import static bot.tg.constant.Core.Response.CONFIRM;
import static bot.tg.constant.Greetings.Callback.*;
import static bot.tg.constant.Greetings.Response.GREETING_TIME;
import static bot.tg.constant.Reminder.Callback.REMINDER_CANCEL;
import static bot.tg.constant.Reminder.Callback.REMINDER_CONFIRM;
import static bot.tg.constant.Reminder.Response.REMINDER_TIME;
import static bot.tg.constant.Reminder.TimePicker.*;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class TimePickerResponseHelper {

    private TimePickerResponseHelper() {}

    public static SendMessage createGreetingTimePickerMessage(TelegramContext context, UserRepository userRepository) {
        Time userPreferredGreetingTime = userRepository.getPreferredGreetingTime(context.userId);
        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildGreetingTimePickerKeyboard(
                userPreferredGreetingTime.getHour(), userPreferredGreetingTime.getMinute()
        );

        return SendMessage.builder()
                .chatId(context.userId)
                .text(GREETING_TIME)
                .replyMarkup(keyboard)
                .build();
    }

    public static EditMessageText createGreetingTimePickerEditMessage(TelegramContext context, UserSession userSession) {
        Time userPreferredGreetingTime = userSession.getMorningGreetingTimeDraft();
        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildGreetingTimePickerKeyboard(
                userPreferredGreetingTime.getHour(), userPreferredGreetingTime.getMinute()
        );

        return EditMessageText.builder()
                .chatId(context.userId)
                .messageId(context.messageId)
                .text(GREETING_TIME)
                .replyMarkup(keyboard)
                .build();
    }

    public static EditMessageText createReminderTimePickerEditMessage(TelegramContext context, ZoneId userTimeZone, UserSession userSession) {
        ReminderCreateDto dto = userSession.getReminderDraft();
        DateTime dateTime = dto.getDateTime();

        ZonedDateTime now = ZonedDateTime.now(userTimeZone);
        if (!dateTime.isTimeManuallyEdited() || now.isAfter(DateTime.DateTimeMapper.toZonedDateTime(dateTime))) {
            dateTime.setHour(now.getHour());
            dateTime.setMinute(now.getMinute());
            dateTime.setTimeZone(userTimeZone.toString());
        }

        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildReminderTimePickerKeyboard(
                dateTime.getHour(), dateTime.getMinute()
        );

        return EditMessageText.builder()
                .chatId(context.userId)
                .messageId(context.messageId)
                .text(REMINDER_TIME)
                .replyMarkup(keyboard)
                .build();
    }

    private static InlineKeyboardMarkup buildReminderTimePickerKeyboard(int hour, int minute) {
        String timeDisplay = String.format("%02d:%02d", hour, minute);

        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();

        InlineKeyboardRow hourRow = new InlineKeyboardRow(
                button(PAGINATION_DOUBLE_PREV, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + MINUS_THREE),
                button(PAGINATION_PREV, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + MINUS_ONE),
                button(timeDisplay, IGNORE),
                button(PAGINATION_NEXT, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + PLUS_ONE),
                button(PAGINATION_DOUBLE_NEXT, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + PLUS_THREE)
        );

        InlineKeyboardRow minuteRow = new InlineKeyboardRow(
                button(REMINDER_TIME_PICKER_MINUS_FIVE_MIN, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + MINUS_FIVE),
                button(REMINDER_TIME_PICKER_MINUS_MIN, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + MINUS_ONE),
                button(REMINDER_TIME_PICKER_PLUS_MIN, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + PLUS_ONE),
                button(REMINDER_TIME_PICKER_PLUS_FIVE_MIN, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + PLUS_FIVE)
        );

        InlineKeyboardRow confirmRow = new InlineKeyboardRow(
                button(CONFIRM, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CONFIRM),
                button(CANCEL, REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CANCEL)
        );

        keyboardRows.add(hourRow);
        keyboardRows.add(minuteRow);
        keyboardRows.add(confirmRow);

        return new InlineKeyboardMarkup(keyboardRows);
    }

    private static InlineKeyboardMarkup buildGreetingTimePickerKeyboard(int hour, int minute) {
        String timeDisplay = String.format("%02d:%02d", hour, minute);

        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();

        InlineKeyboardRow hourRow = new InlineKeyboardRow(
                button(PAGINATION_DOUBLE_PREV, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + MINUS_THREE),
                button(PAGINATION_PREV, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + MINUS_ONE),
                button(timeDisplay, IGNORE),
                button(PAGINATION_NEXT, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + PLUS_ONE),
                button(PAGINATION_DOUBLE_NEXT, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + PLUS_THREE)
        );

        InlineKeyboardRow minuteRow = new InlineKeyboardRow(
                button(REMINDER_TIME_PICKER_MINUS_FIVE_MIN, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + MINUS_FIVE),
                button(REMINDER_TIME_PICKER_MINUS_MIN, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + MINUS_ONE),
                button(REMINDER_TIME_PICKER_PLUS_MIN, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + PLUS_ONE),
                button(REMINDER_TIME_PICKER_PLUS_FIVE_MIN, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + PLUS_THREE)
        );

        InlineKeyboardRow confirmRow = new InlineKeyboardRow(
                button(CONFIRM, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CONFIRM),
                button(CANCEL, GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CANCEL)
        );

        keyboardRows.add(hourRow);
        keyboardRows.add(minuteRow);
        keyboardRows.add(confirmRow);

        return new InlineKeyboardMarkup(keyboardRows);
    }

    private static InlineKeyboardButton button(String text, String data) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(data)
                .build();
    }
}
