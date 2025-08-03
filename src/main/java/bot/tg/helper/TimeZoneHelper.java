package bot.tg.helper;

import bot.tg.dto.ChatContext;
import bot.tg.dto.SupportedTimeZone;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.TimeZone.Button.CHOOSE_TIMEZONE_MANUALLY;
import static bot.tg.constant.TimeZone.Button.SEND_LOCATION;
import static bot.tg.constant.TimeZone.Callback.TIMEZONE;
import static bot.tg.constant.TimeZone.Response.TIMEZONE_CHOICE_MESSAGE;

public class TimeZoneHelper {

    private TimeZoneHelper() {
    }

    public static SendMessage formAdjustTimeZoneMessage(ChatContext chatContext) {
        long chatId = chatContext.getChatId();

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(TIMEZONE_CHOICE_MESSAGE)
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(new KeyboardRow(
                                List.of(
                                        KeyboardButton.builder()
                                                .text(SEND_LOCATION)
                                                .requestLocation(true)
                                                .build(),
                                        KeyboardButton.builder()
                                                .text(CHOOSE_TIMEZONE_MANUALLY)
                                                .build()
                                )
                        )))
                        .resizeKeyboard(true)
                        .oneTimeKeyboard(true)
                        .build())
                .build();
    }

    public static InlineKeyboardMarkup formTimeZoneChoiceKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (SupportedTimeZone timeZone : SupportedTimeZone.values()) {
            if (timeZone == SupportedTimeZone.KYIV_OLD) continue;

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(timeZone.getDisplayName())
                    .callbackData(TIMEZONE + COLON_DELIMITER + timeZone.getZoneId())
                    .build();

            rows.add(new InlineKeyboardRow(button));
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }


}
