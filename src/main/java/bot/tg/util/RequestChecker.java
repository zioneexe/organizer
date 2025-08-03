package bot.tg.util;

import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import org.telegram.telegrambots.meta.api.objects.Update;

import static bot.tg.constant.Symbol.COMMAND_SYMBOL;

public class RequestChecker {

    private RequestChecker() {
    }

    public static boolean isTextMessage(UserRequest request) {
        Update update = request.getUpdate();
        return update.hasMessage() && update.getMessage().hasText();
    }

    public static boolean isCallbackMessage(UserRequest request) {
        return request.getUpdate().hasCallbackQuery();
    }

    public static boolean isCommand(UserRequest request) {
        Update update = request.getUpdate();
        return update.hasMessage() && update.getMessage().isCommand()
                && update.getMessage().hasText() && update.getMessage().getText().startsWith(COMMAND_SYMBOL);
    }

    public static boolean isSticker(UserRequest request) {
        Update update = request.getUpdate();
        return update.hasMessage() && update.getMessage().hasSticker();
    }

    public static boolean isLocation(UserRequest request) {
        Update update = request.getUpdate();
        return update.hasMessage() && update.getMessage().hasLocation();
    }

    public static boolean isStateRequest(UserRequest request) {
        UserSession userSession = request.getUserSession();
        return userSession.getState().compareTo(UserState.IDLE) != 0;
    }

}
