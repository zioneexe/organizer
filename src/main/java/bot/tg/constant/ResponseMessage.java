package bot.tg.constant;

public final class ResponseMessage {

    public static final String START_MESSAGE = "Обери, що тобі потрібно";
    public static final String SETTINGS_MESSAGE = "Гаразд, налаштовуй!";

    public static final String UNKNOWN_COMMAND = "❓ Вибач, я не зрозумів, що ти мав на увазі.\nСпробуй /help.";
    public static final String INVALID_TIME = "⏰ Не можна вибрати час у минулому";
    public static final String ALRIGHT = "Гаразд!";
    public static final String ERROR = "❌ Помилка: ";

    public static final String CREATION_CANCELLED = "Створення скасовано.";
    public static final String TIME_CHANGE_CANCELLED = "Зміна часу скасована.";
    public static final String EDITING_CANCELLED = "Редагування скасовано.";

    public static final String INCORRECT_REQUEST_DELETE = "❌ Некоректний запит на видалення.";
    public static final String INCORRECT_REQUEST_PAGE = "❌ Некоректний запит на зміну сторінки.";
    public static final String INCORRECT_REQUEST_TIMEZONE_CHANGE = "❌ Некоректний запит на зміну часового поясу.";

    private ResponseMessage() {}

}
