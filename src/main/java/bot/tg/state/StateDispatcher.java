package bot.tg.state;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class StateDispatcher {

    private final Map<UserState, StateHandler> handlers = new HashMap<>() {{
        put(UserState.AWAITING_TASK_SELECTION, new TaskSelectionHandler());
        put(UserState.AWAITING_TASK_TITLE, new TaskTitleHandler());
        put(UserState.AWAITING_TASK_DESCRIPTION, new TaskDescriptionHandler());
        put(UserState.AWAITING_REMINDER_TEXT, new ReminderTextHandler());
    }};

    public void dispatch(UserState userState, Update update) {
        StateHandler handler = handlers.get(userState);
        if (handler == null) return;
        handler.handle(update);
    }
}
