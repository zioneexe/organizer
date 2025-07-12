package bot.tg.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static bot.tg.constant.Actions.*;
import static bot.tg.constant.ResponseMessage.START_MESSAGE;

public class MenuHelper {

    public static SendMessage formMenuMessage(long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(START_MESSAGE)
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new KeyboardRow(new KeyboardButton(TASK_SELECTION)),
                                new KeyboardRow(new KeyboardButton(REMINDER_SELECTION)),
                                new KeyboardRow(new KeyboardButton(CONNECT_GOOGLE_CALENDAR))
                        ))
                        .resizeKeyboard(true)
                        .oneTimeKeyboard(true)
                        .build())
                .build();
    }
}
