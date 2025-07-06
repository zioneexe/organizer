package bot.tg.util;

import bot.tg.dto.DateTimeDto;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.provider.ServiceProvider;
import bot.tg.state.UserStateManager;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static bot.tg.util.Constants.*;

public class TimePickerResponseHelper {

    private TimePickerResponseHelper() {}

    public static EditMessageText createTimePickerEditMessage(Update update) {
        UserStateManager userStateManager = ServiceProvider.getUserStateManager();

        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        ReminderCreateDto dto = userStateManager.getReminderDraft(userId);
        DateTimeDto dateTimeDto = dto.getDateTime();

        InlineKeyboardMarkup keyboard = TimePickerResponseHelper.buildTimePickerKeyboard(
                dateTimeDto.getHour(), dateTimeDto.getMinute()
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

        List<InlineKeyboardRow> rows = new ArrayList<>();

        InlineKeyboardRow hourRow = new InlineKeyboardRow(
                button("«", TIME_PICKER + COLON_DELIMITER + CHANGE_HOUR + COLON_DELIMITER + "-1"),
                button(timeDisplay, IGNORE),
                button("»", TIME_PICKER + COLON_DELIMITER + CHANGE_HOUR + COLON_DELIMITER +  "+1")
        );

        InlineKeyboardRow minuteRow = new InlineKeyboardRow(
                button("-1 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "-1"),
                button("-5 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "-5"),
                button("+1 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "+1"),
                button("+5 хв", TIME_PICKER + COLON_DELIMITER + CHANGE_MINUTE + COLON_DELIMITER + "+5")
        );

        InlineKeyboardRow confirmRow = new InlineKeyboardRow(
                button("✅ Підтвердити", TIME_PICKER + COLON_DELIMITER + CONFIRM),
                button("❎ Скасувати", TIME_PICKER + COLON_DELIMITER + CANCEL)
        );

        rows.add(hourRow);
        rows.add(minuteRow);
        rows.add(confirmRow);

        return new InlineKeyboardMarkup(rows);
    }

    private static InlineKeyboardButton button(String text, String data) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(data)
                .build();
    }
}
