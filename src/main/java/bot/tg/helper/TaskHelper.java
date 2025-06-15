package bot.tg.helper;

import bot.tg.model.TaskStatus;
import bot.tg.model.TodoTask;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static bot.tg.Constants.*;

public class TaskHelper {

    private static final Map<TaskStatus, String> STATUS_EMOJIS = new HashMap<>() {{
        put(TaskStatus.IN_PROGRESS, "\uD83D\uDFE8 ");
        put(TaskStatus.COMPLETED, "\uD83D\uDFE9");
    }};

    private static final Map<TaskStatus, String> STATUS_ACTION_EMOJIS = new HashMap<>() {{
        put(TaskStatus.IN_PROGRESS, "✅");
        put(TaskStatus.COMPLETED, "❎");
    }};

    public static Map.Entry<List<List<InlineKeyboardButton>>, String> formDetailsMessage(TodoTask task) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        String title = task.getTitle() != null ? task.getTitle() : "—";
        String description = task.getDescription() != null ? task.getDescription() : "—";
        TaskStatus status = task.getStatus();
        String statusLabel = status == TaskStatus.COMPLETED ? "✅ Виконано" : "🟡 У процесі";

        String builder = "📋 *Деталі завдання:*\n\n" +
                "*Назва:* " + title + "\n" +
                "*Опис:* " + description + "\n" +
                "*Статус:* " + statusLabel;

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData(BACK_TO_TASKS)
                .build());

        keyboardRows.add(row);

        return Map.entry(keyboardRows, builder);
    }

    public static Map.Entry<List<List<InlineKeyboardButton>>, String> formTasksMessage(List<TodoTask> tasks) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> taskStatusButtons = new ArrayList<>();
        List<InlineKeyboardButton> taskDetailsButtons = new ArrayList<>();

        StringBuilder answerBuilder = new StringBuilder("*📝 Ваші завдання на сьогодні:*\n\n");

        AtomicInteger counter = new AtomicInteger(0);
        tasks.forEach(task -> {
            int index = counter.incrementAndGet();
            boolean completed = task.getCompleted();
            TaskStatus taskStatus = completed ? TaskStatus.COMPLETED : TaskStatus.IN_PROGRESS;
            String statusEmoji = STATUS_EMOJIS.get(taskStatus);
            String statusActionEmoji = STATUS_ACTION_EMOJIS.get(taskStatus);
            String callbackStatus = completed ? IN_PROGRESS_TASK : COMPLETED_TASK;

            answerBuilder.append(index)
                    .append(". ")
                    .append(statusEmoji)
                    .append(" ")
                    .append("_").append(escapeMarkdown(task.getTitle())).append("_")
                    .append("\n");

            taskStatusButtons.add(InlineKeyboardButton.builder()
                    .text(index + " " + statusActionEmoji)
                    .callbackData(callbackStatus + ":" + task.getId())
                    .build());

            taskDetailsButtons.add(InlineKeyboardButton.builder()
                    .text(index + " ℹ️")
                    .callbackData(DETAILS_TASK + ":" + task.getId())
                    .build());
        });

        keyboardRows.add(taskStatusButtons);
        keyboardRows.add(taskDetailsButtons);

        String answer = answerBuilder.toString();
        return Map.entry(keyboardRows, answer);
    }

    private static String escapeMarkdown(String text) {
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }

}
