package bot.tg.helper;

import bot.tg.dto.DateTime;
import bot.tg.dto.Time;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserStateManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static bot.tg.constant.Callback.IGNORE;
import static bot.tg.constant.Greetings.Callback.*;
import static bot.tg.constant.Greetings.Response.GREETING_TIME;
import static bot.tg.constant.Reminder.Callback.*;
import static bot.tg.constant.Reminder.Response.REMINDER_TIME;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class TimePickerResponseHelper {

    private TimePickerResponseHelper() {}

    public static SendMessage createGreetingTimePickerMessage(Update update, UserRepository userRepository) {

        long userId = 0;
        long chatId = 0;
        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        Time userPreferredGreetingTime = userRepository.getPreferredGreetingTime(userId);
        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildGreetingTimePickerKeyboard(
                userPreferredGreetingTime.getHour(), userPreferredGreetingTime.getMinute()
        );

        return SendMessage.builder()
                .chatId(chatId)
                .text(GREETING_TIME)
                .replyMarkup(keyboard)
                .build();
    }

    public static EditMessageText createGreetingTimePickerEditMessage(Update update, UserStateManager userStateManager) {

        long userId = 0;
        long chatId = 0;
        int messageId = 0;
        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
            chatId = update.getMessage().getChatId();
            messageId = update.getMessage().getMessageId();
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            messageId = update.getCallbackQuery().getMessage().getMessageId();
        }

        Time userPreferredGreetingTime = userStateManager.getMorningGreetingTimeDraft(userId);
        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildGreetingTimePickerKeyboard(
                userPreferredGreetingTime.getHour(), userPreferredGreetingTime.getMinute()
        );

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(GREETING_TIME)
                .replyMarkup(keyboard)
                .build();
    }

    public static EditMessageText createReminderTimePickerEditMessage(Update update, String userTimeZone, UserStateManager userStateManager) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        ReminderCreateDto dto = userStateManager.getReminderDraft(userId);
        DateTime dateTime = dto.getDateTime();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(userTimeZone));
        if (!dateTime.isTimeManuallyEdited() || now.isAfter(DateTime.DateTimeMapper.toZonedDateTime(dateTime))) {
            dateTime.setHour(now.getHour());
            dateTime.setMinute(now.getMinute());
            dateTime.setTimeZone(userTimeZone);
        }

        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildReminderTimePickerKeyboard(
                dateTime.getHour(), dateTime.getMinute()
        );

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(REMINDER_TIME)
                .replyMarkup(keyboard)
                .build();
    }

    private static InlineKeyboardMarkup buildReminderTimePickerKeyboard(int hour, int minute) {
        String timeDisplay = String.format("%02d:%02d", hour, minute);

        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();

        InlineKeyboardRow hourRow = new InlineKeyboardRow(
                button("<<", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + "-3"),
                button("<", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + "-1"),
                button(timeDisplay, IGNORE),
                button(">", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + "+1"),
                button(">>", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_HOUR + COLON_DELIMITER + "+3")
        );

        InlineKeyboardRow minuteRow = new InlineKeyboardRow(
                button("-5 хв", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + "-5"),
                button("-1 хв", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + "-1"),
                button("+1 хв", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + "+1"),
                button("+5 хв", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CHANGE_MINUTE + COLON_DELIMITER + "+5")
        );

        InlineKeyboardRow confirmRow = new InlineKeyboardRow(
                button("✅ Підтвердити", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CONFIRM),
                button("❎ Скасувати", REMINDER_TIME_PICKER + COLON_DELIMITER + REMINDER_CANCEL)
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
                button("<<", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + "-3"),
                button("<", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + "-1"),
                button(timeDisplay, IGNORE),
                button(">", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + "+1"),
                button(">>", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_HOUR + COLON_DELIMITER + "+3")
        );

        InlineKeyboardRow minuteRow = new InlineKeyboardRow(
                button("-5 хв", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + "-5"),
                button("-1 хв", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + "-1"),
                button("+1 хв", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + "+1"),
                button("+5 хв", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CHANGE_MINUTE + COLON_DELIMITER + "+5")
        );

        InlineKeyboardRow confirmRow = new InlineKeyboardRow(
                button("✅ Підтвердити", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CONFIRM),
                button("❎ Скасувати", GREETING_TIME_PICKER + COLON_DELIMITER + GREETING_CANCEL)
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
