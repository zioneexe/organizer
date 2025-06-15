package bot.tg.provider;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TelegramClientProvider {

    private static TelegramClient instance;

    public static void init(String apiKey) {
        if (instance == null) {
            instance = new OkHttpTelegramClient(apiKey);
        }
    }

    public static TelegramClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Telegram client provider not initialized");
        }
        return instance;
    }
}
