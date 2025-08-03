package bot.tg.handler;

import bot.tg.user.UserRequest;

public interface RequestHandler {

    boolean isApplicable(UserRequest request);

    void handle(UserRequest request);
}
