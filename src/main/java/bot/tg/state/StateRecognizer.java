package bot.tg.state;

import static bot.tg.constant.MainActions.*;
import static bot.tg.constant.SettingsActions.*;
import static bot.tg.constant.TimeZone.Button.CHOOSE_TIMEZONE_MANUALLY;

public class StateRecognizer {

    public static UserState recognize(String text) {
        return switch (text) {
            case TASK_SELECTION -> UserState.AWAITING_TASK_SELECTION;
            case REMINDER_SELECTION -> UserState.AWAITING_REMINDER_SELECTION;
            case SETTINGS -> UserState.SETTINGS;
            case ADJUST_TIMEZONE -> UserState.ADJUSTING_TIMEZONE;
            case CHOOSE_TIMEZONE_MANUALLY -> UserState.TIMEZONE_MANUAL_CHOICE;
            case SWITCH_GOOD_MORNING_OFF, SWITCH_GOOD_MORNING_ON ->  UserState.SWITCH_GOOD_MORNING;
            case CONNECT_GOOGLE_CALENDAR -> UserState.GOOGLE_CONNECT;
            case DISCONNECT_GOOGLE_CALENDAR -> UserState.GOOGLE_DISCONNECT;
            default -> UserState.IDLE;
        };
    }
}
