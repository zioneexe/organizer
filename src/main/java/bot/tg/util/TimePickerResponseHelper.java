package bot.tg.util;

import bot.tg.dto.DateTime;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.provider.ServiceProvider;
import bot.tg.state.UserStateManager;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static bot.tg.constant.Callback.*;
import static bot.tg.constant.Reminder.Callback.*;
import static bot.tg.constant.Reminder.Response.REMINDER_TIME;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class TimePickerResponseHelper {

    private TimePickerResponseHelper() {}

    public static EditMessageText createTimePickerEditMessage(Update update, String userTimeZone) {
        UserStateManager userStateManager = ServiceProvider.getUserStateManager();

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

        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildTimePickerKeyboard(
                dateTime.getHour(), dateTime.getMinute()
        );

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(REMINDER_TIME)
                .replyMarkup(keyboard)
                .build();
    }

    private static InlineKeyboardMarkup buildTimePickerKeyboard(int hour, int minute) {
        String timeDisplay = String.format("%02d:%02d", hour, minute);

        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();

        InlineKeyboardRow hourRow = new InlineKeyboardRow(
                button("<<", TIME_PICKER + COLON_DELIMITER + CHANGE_HOUR + COLON_DELIMITER + "-3"),
                button("<", TIME_PICKER + COLON_DELIMITER + CHANGE_HOUR + COLON_DELIMITER + "-1"),
                button(timeDisplay, IGNORE),
                button(">", TIME_PICKER + COLON_DELIMITER + CHANGE_HOUR + COLON_DELIMITER + "+1"),
                button(">>", TIME_PICKER + COLON_DELIMITER + CHANGE_HOUR + COLON_DELIMITER +  "+3")
        );

        InlineKeyboardRow minuteRow = new InlineKeyboardRow(
                button("-5 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "-5"),
                button("-1 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "-1"),
                button("+1 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "+1"),
                button("+5 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "+5")
        );

        InlineKeyboardRow confirmRow = new InlineKeyboardRow(
                button("✅ Підтвердити", TIME_PICKER + COLON_DELIMITER + CONFIRM),
                button("❎ Скасувати", TIME_PICKER + COLON_DELIMITER + CANCEL)
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
