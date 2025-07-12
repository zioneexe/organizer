package bot.tg.util;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;

public class TelegramHelper {

    private TelegramHelper() {}

    public static <T extends Serializable, Method extends BotApiMethod<T>> void safeExecute(TelegramClient telegramClient, Method method) {
        try {
            telegramClient.execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void safeExecute(TelegramClient telegramClient, SendSticker stickerMessage) {
        try {
            telegramClient.execute(stickerMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void sendSimpleMessage(TelegramClient telegramClient, long userId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId)
                .text(message)
                .build();
        safeExecute(telegramClient, sendMessage);
    }

    public static void sendSimpleCallbackAnswer(TelegramClient telegramClient, String callbackQueryId) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        TelegramHelper.safeExecute(telegramClient, answer);
    }
}
