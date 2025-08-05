package bot.tg.util.validation;

import lombok.Getter;

public class Violation {

    @Getter
    private final String message;

    private Violation(String message) {
        this.message = message;
    }

    public static Violation of(String message) {
        return new Violation(message);
    }
}
