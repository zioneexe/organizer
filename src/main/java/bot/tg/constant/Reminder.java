package bot.tg.constant;

public final class Reminder {

    private Reminder() {}

    public static final class Response {

        public static final String REMINDER_CREATE = "Нове нагадування";
        public static final String REMINDER_DATE = "📅 Обери дату для нагадування:";
        public static final String REMINDER_TIME = "⌛ Оберемо час для нагадування. ";
        public static final String REMINDER_TEXT = "Для чого нагадування? ✨";
        public static final String REMINDER_CREATED = "Вітаю, нагадування створено!";
        public static final String REMINDER_DELETED = "🗑 Нагадування видалено.";
        public static final String REMINDER_NOT_FOUND = "⚠️ Нагадування не знайдено.";
        public static final String REMINDER_ON = "\uD83D\uDD14 Нагадування увімкнено.";
        public static final String REMINDER_OFF = "\uD83D\uDD15 Нагадування вимкнено.";

        public static final String REMINDER_NO_PLANNED = "🔔 Немає запланованих нагадувань.";
        public static final String REMINDER_CURRENT_TIME = "Поточний час: ";
        public static final String REMINDER_YOUR_REMINDERS = "🔔 Ваші нагадування:";

        public static final String REMINDER_TODAY = "Сьогодні";
        public static final String REMINDER_TOMORROW = "Завтра";

        public static final String REMINDER_NOTIFICATION_MESSAGE = "🔔 Нагадування на ";
        public static final String PILLS_REMINDER_MESSAGE = "Ку-ку. Нагадую тобі випити таблетку :) " +
                "Не їж протягом години, бо бабай прийде!";

        private Response() {}

    }

    public static final class Callback {

        public static final String NEW_REMINDER = "reminder_new";
        public static final String DATE_PICKER = "reminder_date_picker";
        public static final String PAGE_REMINDER = "reminder_page";
        public static final String REMINDER_CONFIRM = "reminder_confirm";
        public static final String REMINDER_CANCEL = "reminder_cancel";
        public static final String DELETE_REMINDER = "reminder_delete";
        public static final String ENABLE_REMINDER = "reminder_enable";
        public static final String DISABLE_REMINDER = "reminder_disable";

        private Callback() {}

    }

    public static final class TimePicker {

        public static final String REMINDER_TIME_PICKER_MINUS_FIVE_MIN = "-5 хв";
        public static final String REMINDER_TIME_PICKER_MINUS_MIN = "-1 хв";
        public static final String REMINDER_TIME_PICKER_PLUS_MIN = "+1 хв";
        public static final String REMINDER_TIME_PICKER_PLUS_FIVE_MIN = "+5 хв";

        public static final String MINUS_FIVE = "-5";
        public static final String MINUS_THREE = "-3";
        public static final String MINUS_ONE = "-1";
        public static final String PLUS_ONE = "+1";
        public static final String PLUS_THREE = "+3";
        public static final String PLUS_FIVE = "+5";

        public static final String REMINDER_TIME_PICKER = "reminder_time_picker";
        public static final String REMINDER_CHANGE_HOUR = "reminder_change_hour";
        public static final String REMINDER_CHANGE_MINUTE = "reminder_change_minute";

        private TimePicker() {}

    }
}
