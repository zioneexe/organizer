package bot.tg.constant;

public final class TimeZone {

    private TimeZone() {}

    public static final class Response {

        public static final String TIMEZONE_CHOICE_MESSAGE =
                "📍 Надішли свою локацію, щоб ми визначили твій часовий пояс автоматично\nабо обери вручну:";

        public static final String MANUAL_CHOICE_MESSAGE = "Який часовий пояс?";

        public static final String UNSUPPORTED_TIMEZONE = "⛔ Непідтримуваний часовий пояс";
        public static final String TIMEZONE_CHANGE = "✅ Твій часовий пояс змінено на:\n";
        public static final String TIMEZONE_DETECTED_AUTOMATICALLY = "✅ Твій часовий пояс визначено автоматично: ";

        private Response() {}

    }

    public static final class Button {

        public static final String SEND_LOCATION = "📍 Надіслати локацію";
        public static final String CHOOSE_TIMEZONE_MANUALLY = "🕒 Обрати вручну";

        private Button() {}

    }

    public static final class Callback {

        public static final String TIMEZONE = "timezone";

        private Callback() {}

    }

}
