package bot.tg.util.validation;

public class Rules {

    private Rules() {
    }

    public static Rule<String> required(String fieldName) {
        return value -> (value == null || value.isBlank())
                ? Violation.of(fieldName + " обовʼязковий. 🙏")
                : null;
    }

    public static Rule<String> maxLength(int max, String errorMessage) {
        return value -> (value != null && value.length() > max)
                ? Violation.of(errorMessage)
                : null;
    }

}
