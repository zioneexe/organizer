package bot.tg.handler;

import bot.tg.helper.TelegramHelper;
import bot.tg.service.StickerService;
import bot.tg.user.UserRequest;
import bot.tg.util.RequestChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class StickerHandler implements RequestHandler {

    private final StickerService stickerService;
    private final TelegramClient telegramClient;

    @Override
    public boolean isApplicable(UserRequest request) {
        return RequestChecker.isSticker(request);
    }

    @Override
    public void handle(UserRequest request) {
        SendSticker sendSticker = stickerService.sendSticker(request);
        TelegramHelper.safeExecute(telegramClient, sendSticker);
    }
}
