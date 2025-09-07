package bot.tg.helper;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.model.TaskStatus;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.service.TimeZoneService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static bot.tg.constant.Core.Pagination.*;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Symbol.NO_INFO;
import static bot.tg.constant.Task.Callback.*;
import static bot.tg.constant.Task.Response.*;
import static bot.tg.util.MarkupAdjuster.escapeMarkdown;

public class TaskHelper {

    private static final String TASK_DETAILS_TEMPLATE =
            """
                    📋 *Деталі завдання:*
                    
                    *Назва:* {0}
                    *Опис:* {1}
                    *Статус:* {2}
                    """;

    private static final Map<TaskStatus, String> STATUS_EMOJIS = new HashMap<>() {{
        put(TaskStatus.IN_PROGRESS, "\uD83D\uDFE8 ");
        put(TaskStatus.COMPLETED, "\uD83D\uDFE9");
    }};

    private static final Map<TaskStatus, String> STATUS_ACTION_EMOJIS = new HashMap<>() {{
        put(TaskStatus.IN_PROGRESS, "✅");
        put(TaskStatus.COMPLETED, "❎");
    }};

    private TaskHelper() {
    }

    public static Map.Entry<InlineKeyboardMarkup, String> formDetailsMessage(TodoTask task) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();

        String title = task.getTitle() != null ? task.getTitle() : NO_INFO;
        String description = task.getDescription() != null ? task.getDescription() : NO_INFO;
        TaskStatus status = task.getStatus();
        String statusLabel = status == TaskStatus.COMPLETED ? TASK_COMPLETED_DETAILS : TASK_IN_PROGRESS_DETAILS;
        String detailsMessage = MessageFormat.format(TASK_DETAILS_TEMPLATE, title, description, statusLabel);

        List<InlineKeyboardButton> actionButtonsRow = new ArrayList<>();

        actionButtonsRow.add(InlineKeyboardButton.builder()
                .text(TASK_GO_BACK)
                .callbackData(BACK_TO_TASKS)
                .build());

        keyboardRows.add(new InlineKeyboardRow(actionButtonsRow));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
        return Map.entry(keyboard, detailsMessage);
    }

    public static Map.Entry<InlineKeyboardMarkup, String> formTasksMessage(List<TodoTask> tasks, Pageable pageable) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        if (tasks.isEmpty()) {
            keyboardRows.add(new InlineKeyboardRow(formTaskEditingKeyboard()));
            return Map.entry(InlineKeyboardMarkup.builder().keyboard(keyboardRows).build(), TASK_NOTHING_FOR_TODAY);
        }

        int currentPage = pageable.getPage();
        int totalPages = pageable.getTotalPages();

        StringBuilder answerBuilder = new StringBuilder().append("*")
                .append(TASK_YOUR_TASKS)
                .append("*\n\n");
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

            keyboardRows.add(new InlineKeyboardRow(taskActionButtons));
        });

        answerBuilder.append(PAGINATION_PAGE).append(currentPage).append(PAGINATION_DIVIDER).append(totalPages);

        List<InlineKeyboardButton> actionButtons = new ArrayList<>();
        actionButtons.add(InlineKeyboardButton.builder()
                .text(PAGINATION_PREV)
                .callbackData(PAGE_TASK + COLON_DELIMITER + Math.max(currentPage - 1, 1))
                .build());
        actionButtons.add(InlineKeyboardButton.builder()
                .text(TASK_CREATE)
                .callbackData(NEW_TASK)
                .build());
        actionButtons.add(InlineKeyboardButton.builder()
                .text(PAGINATION_NEXT)
                .callbackData(PAGE_TASK + COLON_DELIMITER + Math.min(currentPage + 1, totalPages))
                .build());

        keyboardRows.add(new InlineKeyboardRow(actionButtons));

        return Map.entry(InlineKeyboardMarkup.builder().keyboard(keyboardRows).build(), answerBuilder.toString());
    }

    public static InlineKeyboardButton formTaskEditingKeyboard() {
        return InlineKeyboardButton.builder()
                .text(TASK_CREATE)
                .callbackData(NEW_TASK)
                .build();
    }


    public static InlineKeyboardMarkup formTaskEditingKeyboard(String taskId) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(TASK_EDIT_TITLE_LABEL)
                                .callbackData(EDIT_NAME_TASK + COLON_DELIMITER + taskId)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(TASK_EDIT_DESCRIPTION_LABEL)
                                .callbackData(EDIT_DESCRIPTION_TASK + COLON_DELIMITER + taskId)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(TASK_EDIT_DELETE_LABEL)
                                .callbackData(DELETE_TASK + COLON_DELIMITER + taskId)
                                .build(),
                        InlineKeyboardButton.builder()
                                .text(TASK_EDIT_CANCEL_LABEL)
                                .callbackData(CANCEL_EDIT_TASK)
                                .build()
                ))
                .build();
    }

    public static SendMessage createTasksMessage(UserRequest request,
                                                 TimeZoneService timeZoneService,
                                                 TaskRepository taskRepository,
                                                 Pageable pageable,
                                                 LocalDate chosenDate) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        userSession.setCurrentTaskPage(1);
        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        List<TodoTask> tasks = taskRepository.getByUserForDayPaged(context.userId, pageable, chosenDate, userZoneId);
        Map.Entry<InlineKeyboardMarkup, String> tasksMessage = TaskHelper.formTasksMessage(tasks, pageable);
        InlineKeyboardMarkup keyboard = tasksMessage.getKey();
        String answer = tasksMessage.getValue();

        return SendMessage.builder()
                .chatId(context.userId)
                .text(answer)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

    public static EditMessageText createTasksEditMessage(UserRequest request,
                                                         TimeZoneService timeZoneService,
                                                         TaskRepository taskRepository,
                                                         Pageable pageable,
                                                         LocalDate chosenDate) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);

        userSession.setCurrentTaskPage(pageable.getPage());
        List<TodoTask> updatedTasks = taskRepository.getByUserForDayPaged(context.userId, pageable, chosenDate, userZoneId);
        Map.Entry<InlineKeyboardMarkup, String> updatedTasksMessage = TaskHelper.formTasksMessage(updatedTasks, pageable);
        InlineKeyboardMarkup keyboard = updatedTasksMessage.getKey();
        String editAnswer = updatedTasksMessage.getValue();

        return EditMessageText.builder()
                .chatId(context.userId)
                .messageId(context.messageId)
                .text(editAnswer)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

}
