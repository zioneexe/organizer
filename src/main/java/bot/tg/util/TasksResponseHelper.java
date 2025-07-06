package bot.tg.util;

import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;

public class TasksResponseHelper {

    private TasksResponseHelper() {}

    public static SendMessage createTasksMessage(TaskRepository taskRepository, Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();

        List<TodoTask> tasks = taskRepository.getForTodayByUserId(userId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> tasksMessage = TaskHelper.formTasksMessage(tasks);
        List<List<InlineKeyboardButton>> keyboardRows = tasksMessage.getKey();
        String answer = tasksMessage.getValue();

        return SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(keyboardRows.isEmpty() ? List.of() : List.of(
                                new InlineKeyboardRow(keyboardRows.get(0)),
                                new InlineKeyboardRow(keyboardRows.get(1))
                        ))
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

    public static EditMessageText createTasksEditMessage(TaskRepository taskRepository, Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        List<TodoTask> updatedTasks = taskRepository.getForTodayByUserId(userId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> updatedTasksMessage = TaskHelper.formTasksMessage(updatedTasks);
        List<List<InlineKeyboardButton>> keyboardRows = updatedTasksMessage.getKey();
        String editAnswer = updatedTasksMessage.getValue();

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(editAnswer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new InlineKeyboardRow(keyboardRows.get(0)),
                                new InlineKeyboardRow(keyboardRows.get(1))
                        ))
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }
}
