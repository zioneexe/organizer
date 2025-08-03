package bot.tg.handler.state;

import bot.tg.handler.RequestHandler;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.util.RequestChecker;

import java.util.Set;

public abstract class StateHandler implements RequestHandler {

    public abstract Set<UserState> getSupportedStates();

    @Override
    public boolean isApplicable(UserRequest request) {
        UserState state = request.getUserSession().getState();
        return RequestChecker.isStateRequest(request) && getSupportedStates().contains(state);
    }
}
