package bot.tg.util;

import org.telegram.telegrambots.meta.api.objects.Update;

import static bot.tg.constant.Symbol.COMMAND_SYMBOL;

public class RequestChecker {

    private RequestChecker() {
    }

    public static boolean isTextMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    public static boolean isCommand(Update update) {
        return update.hasMessage() && update.getMessage().isCommand()
                && update.getMessage().hasText() && update.getMessage().getText().startsWith(COMMAND_SYMBOL);
    }

    public static boolean isSticker(Update update) {
        return update.hasMessage() && update.getMessage().hasSticker();
    }

    public static boolean isLocation(Update update) {
        return update.hasMessage() && update.getMessage().hasLocation();
    }

}
