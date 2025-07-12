package bot.tg.callback;

import bot.tg.provider.TelegramClientProvider;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Callback.IGNORE;

public class IgnoreHandler implements CallbackHandler {

    @Override
    public boolean supports(String data) {
        return data.equals(IGNORE);
    }

    @Override
    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        String callbackQueryId = update.getCallbackQuery().getId();
        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
