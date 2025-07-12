package bot.tg.util;

import bot.tg.dto.ChatContext;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
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

    public static ChatContext extractChatContext(Update update) {
        if (update.hasMessage()) {
            return new ChatContext(
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getChatId()
            );
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            return new ChatContext(
                    query.getFrom().getId(),
                    query.getMessage().getChatId()
            );
        }

        return null;
    }

    public static void sendEditMessage(TelegramClient telegramClient, int messageId, long chatId, String message) {
        EditMessageText editMessage = EditMessageText.builder()
                .messageId(messageId)
                .chatId(chatId)
                .text(message)
                .build();
        TelegramHelper.safeExecute(telegramClient, editMessage);
    }

    public static void sendMessageWithKeyboardRemove(TelegramClient telegramClient, long userId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId)
                .text(message)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
        safeExecute(telegramClient, sendMessage);
    }

    public static void sendMessageWithForceReply(TelegramClient telegramClient, long userId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId)
                .text(message)
                .replyMarkup(ForceReplyKeyboard.builder().forceReply(true).build())
                .build();
        safeExecute(telegramClient, sendMessage);
    }

    public static void sendSimpleMessage(TelegramClient telegramClient, long userId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId)
                .text(message)
                .build();
        safeExecute(telegramClient, sendMessage);
    }

    public static void sendMessageWithMarkup(TelegramClient telegramClient, long chatId, String message, ReplyKeyboard markup) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(markup)
                .build();
        safeExecute(telegramClient, sendMessage);
    }

    public static void sendCallbackAnswerWithMessage(TelegramClient telegramClient, String callbackQueryId, String message) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(message)
                .build();
        safeExecute(telegramClient, answer);
    }

    public static void sendCallbackAnswerWithMessageAlert(TelegramClient telegramClient, String callbackQueryId, String message) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(message)
                .showAlert(true)
                .build();
        safeExecute(telegramClient, answer);
    }

    public static void sendSimpleCallbackAnswer(TelegramClient telegramClient, String callbackQueryId) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        safeExecute(telegramClient, answer);
    }
}
