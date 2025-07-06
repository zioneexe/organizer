package bot.tg.util;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;

public class TelegramHelper {

    public static <T extends Serializable, Method extends BotApiMethod<T>> void safeExecute(TelegramClient telegramClient, Method method) {
        try {
            telegramClient.execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
