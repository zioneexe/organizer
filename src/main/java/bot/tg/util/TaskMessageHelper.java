package bot.tg.util;

import bot.tg.model.TaskStatus;
import bot.tg.model.TodoTask;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static bot.tg.constant.Callback.*;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.*;
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

    public static Map.Entry<List<List<InlineKeyboardButton>>, String> formTasksMessage(List<TodoTask> tasks) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (tasks.isEmpty()) {
            List<InlineKeyboardButton> addNewTaskButton = new ArrayList<>();

            addNewTaskButton.add(InlineKeyboardButton.builder()
                    .text("Нове завдання")
                    .callbackData("new_task")
                    .build());

            keyboardRows.add(addNewTaskButton);

            return Map.entry(keyboardRows, "📝 Завдань на сьогодні поки немає.");
        }

        StringBuilder answerBuilder = new StringBuilder("*📝 Ваші завдання на сьогодні:*\n\n");

        AtomicInteger counter = new AtomicInteger(0);
        List<InlineKeyboardButton> taskActionButtons = new ArrayList<>();
        tasks.forEach(task -> {
            int index = counter.incrementAndGet();
            boolean completed = task.getCompleted();
            TaskStatus taskStatus = completed ? TaskStatus.COMPLETED : TaskStatus.IN_PROGRESS;
            String statusEmoji = STATUS_EMOJIS.get(taskStatus);
            String statusActionEmoji = STATUS_ACTION_EMOJIS.get(taskStatus);
            String statusCallback = completed ? IN_PROGRESS_TASK : COMPLETED_TASK;

            answerBuilder.append(index)
                    .append(". ")
                    .append(statusEmoji)
                    .append(" ")
                    .append("_").append(escapeMarkdown(task.getTitle())).append("_")
                    .append("\n");

            taskActionButtons.add(InlineKeyboardButton.builder()
                    .text(index + " " + statusActionEmoji)
                    .callbackData(statusCallback + COLON_DELIMITER + task.getId())
                    .build());

            taskActionButtons.add(InlineKeyboardButton.builder()
                    .text(index + " ℹ️")
                    .callbackData(EDIT_TASK + COLON_DELIMITER + task.getId())
                    .build());

            taskActionButtons.add(InlineKeyboardButton.builder()
                    .text(index + " ℹ️")
                    .callbackData(DETAILS_TASK + COLON_DELIMITER + task.getId())
                    .build());
        });

        List<InlineKeyboardButton> paginationButtons = new ArrayList<>();

        paginationButtons.add(InlineKeyboardButton.builder()
                .text("Prev")
                .callbackData("")
                .build());
        paginationButtons.add(InlineKeyboardButton.builder()
                .text(",")
                .callbackData(IGNORE)
                .build());
        paginationButtons.add(InlineKeyboardButton.builder()
                .text("Next")
                .callbackData("")
                .build());

        keyboardRows.add(paginationButtons);
        keyboardRows.add(taskActionButtons);

        return Map.entry(keyboardRows, answerBuilder.toString());
    }

}
