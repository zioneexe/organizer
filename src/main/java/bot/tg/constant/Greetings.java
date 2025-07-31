package bot.tg.constant;

public final class Greetings {

    public static String GREETINGS_CHOICE_MESSAGE = "Обери час для привітань або вимкни чи увімкни їх за потреби.";

    private Greetings() {
    }

    public static final class Button {

        public static final String SWITCH_GREETING_ON = "\uD83C\uDF1E Увімкнути ранкові привітання";
        public static final String SWITCH_GREETING_OFF = "\uD83C\uDF1A Вимкнути ранкові привітання";
        public static final String CHOOSE_APPROPRIATE_TIME = "🕒 Обрати зручний час";

        private Button() {
        }

    }

    public static final class Response {

        public static final String GREETING_TIME = "⌛ Оберемо час для привітання. ";
        public static final String GREETING_TIME_SET = "✨ Вітаю, встановлено новий час для привітань: ";
        private Response() {
        }

    }

    public static final class Callback {

        public static final String GREETING_TIME_PICKER = "greeting_time_picker";
        public static final String GREETING_CHANGE_HOUR = "greeting_change_hour";
        public static final String GREETING_CHANGE_MINUTE = "greeting_change_minute";
        public static final String GREETING_CONFIRM = "greeting_confirm";
        public static final String GREETING_CANCEL = "greeting_cancel";
        private Callback() {
        }

    }

}
