package bot.tg.handler.command;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.RequestHandler;
import bot.tg.user.UserRequest;
import bot.tg.util.RequestChecker;

public abstract class BotCommand implements RequestHandler {

    public abstract String getCommand();

    @Override
    public boolean isApplicable(UserRequest request) {
        TelegramContext context = request.getContext();
        if (context.text == null) return false;

        return RequestChecker.isCommand(request) && context.text.equals(getCommand());
    }
}
