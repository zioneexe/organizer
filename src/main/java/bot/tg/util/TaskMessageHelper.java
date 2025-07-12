package bot.tg.util;

import bot.tg.dto.Pageable;
import bot.tg.model.TaskStatus;
import bot.tg.model.TodoTask;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.*;
import static bot.tg.constant.Task.Response.TASK_CREATE;
import static bot.tg.util.TextHelper.escapeMarkdown;

public class TaskMessageHelper {

    private TaskMessageHelper() {}

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

        List<InlineKeyboardButton> actionButtonsRow = new ArrayList<>();

        actionButtonsRow.add(InlineKeyboardButton.builder()
                .text("🔙 Назад")
                .callbackData(BACK_TO_TASKS)
                .build());
        actionButtonsRow.add(InlineKeyboardButton.builder()
                .text("🗑")
                .callbackData(DELETE_TASK + COLON_DELIMITER + task.getId())
                .build());

        keyboardRows.add(actionButtonsRow);

        return Map.entry(keyboardRows, builder);
    }

    public static Map.Entry<List<List<InlineKeyboardButton>>, String> formTasksMessage(List<TodoTask> tasks, Pageable pageable) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        if (tasks.isEmpty()) {
            keyboardRows.add(List.of(formNewTaskButton()));
            return Map.entry(keyboardRows, "📝 Завдань на сьогодні поки немає.");
        }

        int currentPage = pageable.getPage();
        int totalPages = pageable.getTotalPages();

        StringBuilder answerBuilder = new StringBuilder("*📝 Ваші завдання на сьогодні:*\n\n");
        AtomicInteger counter = new AtomicInteger(0);
        tasks.forEach(task -> {
            List<InlineKeyboardButton> taskActionButtons = new ArrayList<>();

            int index = counter.incrementAndGet();
            boolean completed = task.getCompleted();
            TaskStatus taskStatus = completed ? TaskStatus.COMPLETED : TaskStatus.IN_PROGRESS;
            String statusEmoji = STATUS_EMOJIS.get(taskStatus);
            String statusActionEmoji = STATUS_ACTION_EMOJIS.get(taskStatus);
            String statusCallback = completed ? IN_PROGRESS_TASK : COMPLETED_TASK;

            answerBuilder.append(index)
                    .append(".    ")
                    .append(statusEmoji)
                    .append("   `").append(escapeMarkdown(task.getTitle())).append("`")
                    .append("\n\n");

            taskActionButtons.add(InlineKeyboardButton.builder()
                    .text(index + " " + statusActionEmoji)
                    .callbackData(statusCallback + COLON_DELIMITER + task.getId())
                    .build());

            taskActionButtons.add(InlineKeyboardButton.builder()
                    .text(index + " ✏️")
                    .callbackData(EDIT_TASK + COLON_DELIMITER + task.getId())
                    .build());

            taskActionButtons.add(InlineKeyboardButton.builder()
                    .text(index + " ℹ️")
                    .callbackData(DETAILS_TASK + COLON_DELIMITER + task.getId())
                    .build());

            keyboardRows.add(taskActionButtons);
        });

        answerBuilder.append("Сторінка ").append(currentPage).append(" / ").append(totalPages);

        List<InlineKeyboardButton> actionButtons = new ArrayList<>();
        actionButtons.add(InlineKeyboardButton.builder()
                .text("<")
                .callbackData(PAGE_TASK + COLON_DELIMITER + Math.max(currentPage - 1, 1))
                .build());
        actionButtons.add(InlineKeyboardButton.builder()
                .text(TASK_CREATE)
                .callbackData(NEW_TASK)
                .build());
        actionButtons.add(InlineKeyboardButton.builder()
                .text(">")
                .callbackData(PAGE_TASK + COLON_DELIMITER + Math.min(currentPage + 1, totalPages))
                .build());

        keyboardRows.add(actionButtons);

        return Map.entry(keyboardRows, answerBuilder.toString());
    }

    public static InlineKeyboardButton formNewTaskButton() {
        return InlineKeyboardButton.builder()
                .text(TASK_CREATE)
                .callbackData(NEW_TASK)
                .build();
    }

}
