package bot.tg.handler.callback.handler;

import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Callback.IGNORE;

@Component
@RequiredArgsConstructor
public class IgnoreHandler extends CallbackHandler {

    private final TelegramClient telegramClient;

    @Override
    public boolean supports(String data) {
        return data.equals(IGNORE);
    }

    @Override
    public void handle(UserRequest request) {
        String callbackQueryId = request.getUpdate().getCallbackQuery().getId();
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
