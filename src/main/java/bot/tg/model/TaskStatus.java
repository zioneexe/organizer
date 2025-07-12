package bot.tg.model;

import static bot.tg.constant.Task.Callback.COMPLETED_TASK;
import static bot.tg.constant.Task.Callback.IN_PROGRESS_TASK;

public enum TaskStatus {
    IN_PROGRESS,
    COMPLETED;

    public boolean toBoolean() {
        return this == COMPLETED;
    }

    public static TaskStatus fromBoolean(boolean value) {
        return value ? COMPLETED : IN_PROGRESS;
    }

    public static TaskStatus fromString(String value) {
        return switch (value) {
            case IN_PROGRESS_TASK -> IN_PROGRESS;
            case COMPLETED_TASK -> COMPLETED;
            default -> null;
        };
    }
}
