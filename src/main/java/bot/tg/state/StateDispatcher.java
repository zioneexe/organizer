package bot.tg.state;

import bot.tg.state.handler.*;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class StateDispatcher {

    private final Map<UserState, StateHandler> handlers = new HashMap<>() {{
        put(UserState.SETTINGS, new SettingsHandler());
        put(UserState.ADJUSTING_TIMEZONE, new TimeZoneAdjustmentHandler());
        put(UserState.SWITCH_GREETING, new GreetingsSwitchHandler());
        put(UserState.GOOGLE_CONNECT, new GoogleConnectHandler());
        put(UserState.GOOGLE_DISCONNECT, new GoogleDisconnectHandler());
        put(UserState.AWAITING_TASK_SELECTION, new TaskSelectionHandler());
        put(UserState.AWAITING_REMINDER_SELECTION, new ReminderSelectionHandler());
        put(UserState.AWAITING_TASK_TITLE, new TaskTitleHandler());
        put(UserState.AWAITING_TASK_DESCRIPTION, new TaskDescriptionHandler());
        put(UserState.AWAITING_REMINDER_TEXT, new ReminderTextHandler());
        put(UserState.AWAITING_LOCATION, new LocationReceiveHandler());
        put(UserState.TIMEZONE_MANUAL_CHOICE, new ManualTimeZoneChoiceHandler());
        put(UserState.ADJUSTING_GREETINGS, new GreetingsAdjustmentHandler());
        put(UserState.CHOOSING_GREETINGS_TIME, new GreetingsTimeChoiceHandler());
        put(UserState.EDITING_TASK_NAME, new TaskEditingHandler());
        put(UserState.EDITING_TASK_DESCRIPTION, new TaskEditingHandler());
    }};

    public void dispatch(UserState userState, Update update) {
        StateHandler handler = handlers.get(userState);
        if (handler == null) return;
        handler.handle(update);
    }
}
