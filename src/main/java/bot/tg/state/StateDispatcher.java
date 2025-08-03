package bot.tg.state;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StateDispatcher {

    private final Map<UserState, StateHandler> handlers = new HashMap<>();

    public StateDispatcher(List<StateHandler> handlers) {
        for (StateHandler stateHandler : handlers) {
            stateHandler.getSupportedStates().forEach(state -> this.handlers.put(state, stateHandler));
        }
    }

    public void dispatch(UserState userState, Update update) {
        StateHandler handler = handlers.get(userState);
        if (handler == null) return;
        handler.handle(update);
    }
}
