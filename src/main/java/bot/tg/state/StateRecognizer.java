package bot.tg.state;

import static bot.tg.util.Constants.REMINDER_SELECTION;
import static bot.tg.util.Constants.TASK_SELECTION;

public class StateRecognizer {

    public static UserState recognize(String text) {
        return switch (text) {
            case TASK_SELECTION -> UserState.AWAITING_TASK_SELECTION;
            case REMINDER_SELECTION -> UserState.AWAITING_REMINDER_SELECTION;
            default -> UserState.IDLE;
        };
    }
}
