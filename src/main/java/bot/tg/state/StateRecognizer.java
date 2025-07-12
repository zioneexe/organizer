package bot.tg.state;

import static bot.tg.constant.Actions.*;

public class StateRecognizer {

    public static UserState recognize(String text) {
        return switch (text) {
            case TASK_SELECTION -> UserState.AWAITING_TASK_SELECTION;
            case REMINDER_SELECTION -> UserState.AWAITING_REMINDER_SELECTION;
            case CONNECT_GOOGLE_CALENDAR -> UserState.GOOGLE_CONNECT;
            default -> UserState.IDLE;
        };
    }
}
