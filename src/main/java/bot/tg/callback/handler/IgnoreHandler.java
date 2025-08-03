package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Callback.IGNORE;

@Component
@RequiredArgsConstructor
public class IgnoreHandler implements CallbackHandler {

    private final TelegramClient telegramClient;

    @Override
    public boolean supports(String data) {
        return data.equals(IGNORE);
    }

    @Override
    public void handle(Update update) {
        String callbackQueryId = update.getCallbackQuery().getId();
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
