package bot.tg.helper;

import bot.tg.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static bot.tg.constant.MainActions.*;
import static bot.tg.constant.ResponseMessage.SETTINGS_MESSAGE;
import static bot.tg.constant.ResponseMessage.START_MESSAGE;
import static bot.tg.constant.SettingsActions.*;

public class MenuHelper {

    private MenuHelper() {
    }

    public static SendMessage formMenuMessage(Long userId) {

        return SendMessage.builder()
                .chatId(userId)
                .text(START_MESSAGE)
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new KeyboardRow(new KeyboardButton(TASK_SELECTION)),
                                new KeyboardRow(new KeyboardButton(REMINDER_SELECTION)),
                                new KeyboardRow(new KeyboardButton(SETTINGS))
                        ))
                        .resizeKeyboard(true)
                        .oneTimeKeyboard(true)
                        .build())
                .build();
    }

    public static SendMessage formSettingsMenu(UserRepository userRepository, Long userId) {
        boolean isGoogleConnected = userRepository.isGoogleConnected(userId);

        return SendMessage.builder()
                .chatId(userId)
                .text(SETTINGS_MESSAGE)
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new KeyboardRow(new KeyboardButton(isGoogleConnected ? DISCONNECT_GOOGLE_CALENDAR : CONNECT_GOOGLE_CALENDAR)),
                                new KeyboardRow(new KeyboardButton(ADJUST_GREETINGS)),
                                new KeyboardRow(new KeyboardButton(ADJUST_TIMEZONE))
                        ))
                        .resizeKeyboard(true)
                        .oneTimeKeyboard(true)
                        .build())
                .build();
    }
}
