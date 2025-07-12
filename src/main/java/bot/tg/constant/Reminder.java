package bot.tg.constant;

public final class Reminder {

    private Reminder() {}

    public static final class Response {

        private Response() {}

        public static final String REMINDER_CREATE = "Нове нагадування";
        public static final String REMINDER_DATE = "📅 Обери дату для нагадування:";
        public static final String REMINDER_TIME = "⌛ Оберемо час для нагадування. ";
        public static final String REMINDER_TEXT = "Для чого нагадування? ✨";
        public static final String REMINDER_CREATED = "Вітаю, нагадування створено!";

    }

    public static final class Callback {

        private Callback() {}

        public static final String NEW_REMINDER = "reminder_new";
        public static final String DATE_PICKER = "reminder_date_picker";
        public static final String TIME_PICKER = "reminder_time_picker";
        public static final String CHANGE_HOUR = "reminder_change_hour";
        public static final String CHANGE_MINUTE = "reminder_change_minute";
        public static final String DELETE_REMINDER = "reminder_delete";

    }
}
