package bot.tg.constant;

public final class Core {

    public static int DEFAULT_PAGE_SIZE = 4;
    public static String DEFAULT_CALENDAR_ID = "primary";

    private Core() {}

    public static final class Response {

        public static final String CONFIRM = "✅ Підтвердити";
        public static final String CANCEL = "❎ Скасувати";
        public static final String ALRIGHT = "Гаразд!";
        public static final String ERROR = "❌ Помилка: ";

        private Response() {}

    }

    public static final class Message {

        public static final String BOT_STARTED = "✅ Бот Organizer успішно стартанув!";
        public static final String BOT_STOPPED = "⏹️ Бот Organizer завершує свою роботу.";

        private Message() {}

    }

    public static final class Pagination {

        public static final String PAGINATION_PAGE = "Сторінка ";
        public static final String PAGINATION_DIVIDER = " / ";
        public static final String PAGINATION_PREV = "<";
        public static final String PAGINATION_NEXT = ">";
        public static final String PAGINATION_DOUBLE_PREV = "<<";
        public static final String PAGINATION_DOUBLE_NEXT = ">>";

        private Pagination() {}

    }
}
