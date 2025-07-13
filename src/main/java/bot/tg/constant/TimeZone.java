package bot.tg.constant;

public final class TimeZone {

    private TimeZone() {}

    public static final class Response {

        private Response() {}

        public static final String TIMEZONE_CHOICE_MESSAGE =
                "📍 Надішли свою локацію, щоб ми визначили твій часовий пояс автоматично\nабо обери вручну:";

        public static final String MANUAL_CHOICE_MESSAGE = "Який часовий пояс?";

    }

    public static final class Button {

        private Button() {}

        public static final String SEND_LOCATION = "📍 Надіслати локацію";
        public static final String CHOOSE_TIMEZONE_MANUALLY = "🕒 Обрати вручну";
    }

    public static final class Callback {

        private Callback() {}

        public static final String TIMEZONE = "timezone";
    }

}
