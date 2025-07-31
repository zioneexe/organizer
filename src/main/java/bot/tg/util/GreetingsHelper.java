package bot.tg.util;

import bot.tg.dto.ChatContext;
import bot.tg.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static bot.tg.constant.Greetings.Button.*;
import static bot.tg.constant.Greetings.GREETINGS_CHOICE_MESSAGE;

public class GreetingsHelper {

    public static SendMessage formAdjustGreetingsMessage(UserRepository userRepository, ChatContext chatContext) {
        long chatId = chatContext.getChatId();
        long userId = chatContext.getUserId();

        boolean greetingsEnabled = userRepository.greetingsEnabled(userId);

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(GREETINGS_CHOICE_MESSAGE)
                .replyMarkup(ReplyKeyboardMarkup.builder()
                        .keyboard(List.of(new KeyboardRow(
                                List.of(
                                        KeyboardButton.builder()
                                                .text(greetingsEnabled ? SWITCH_GREETING_OFF : SWITCH_GREETING_ON)
                                                .build(),
                                        KeyboardButton.builder()
                                                .text(CHOOSE_APPROPRIATE_TIME)
                                                .build()
                                )
                        )))
                        .resizeKeyboard(true)
                        .oneTimeKeyboard(true)
                        .build())
                .build();
    }
}
