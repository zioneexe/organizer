package bot.tg.handler.callback;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.RequestHandler;
import bot.tg.user.UserRequest;
import bot.tg.util.RequestChecker;

public abstract class CallbackHandler implements RequestHandler {

    public abstract boolean supports(String data);

    @Override
    public boolean isApplicable(UserRequest request) {
        TelegramContext context = request.getContext();
        if (context.data == null) return false;

        return RequestChecker.isCallbackMessage(request) && supports(context.data);
    }
}
