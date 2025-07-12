package bot.tg.util;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserStateManager;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class TasksResponseHelper {

    private TasksResponseHelper() {}

    public static SendMessage createTasksMessage(UserStateManager userStateManager,
                                                 UserRepository userRepository,
                                                 TaskRepository taskRepository,
                                                 Pageable pageable,
                                                 ChatContext chatContext,
                                                 LocalDate chosenDate) {
        long userId = chatContext.getUserId();
        long chatId = chatContext.getChatId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        userStateManager.setCurrentTaskPage(userId, 1);
        List<TodoTask> tasks = taskRepository.getByUserForDayPaged(userId, pageable, chosenDate, userZoneId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> tasksMessage = TaskMessageHelper.formTasksMessage(tasks, pageable);
        List<List<InlineKeyboardButton>> keyboardRows = tasksMessage.getKey();
        String answer = tasksMessage.getValue();

        return SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(keyboardRows.isEmpty() ? List.of() : keyboardRows.stream()
                                .map(InlineKeyboardRow::new)
                                .toList())
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

    public static EditMessageText createTasksEditMessage(UserStateManager userStateManager,
                                                         UserRepository userRepository,
                                                         TaskRepository taskRepository,
                                                         Pageable pageable,
                                                         LocalDate chosenDate,
                                                         Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        userStateManager.setCurrentTaskPage(userId, pageable.getPage());
        List<TodoTask> updatedTasks = taskRepository.getByUserForDayPaged(userId, pageable, chosenDate, userZoneId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> updatedTasksMessage = TaskMessageHelper.formTasksMessage(updatedTasks, pageable);
        List<List<InlineKeyboardButton>> keyboardRows = updatedTasksMessage.getKey();
        String editAnswer = updatedTasksMessage.getValue();

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(editAnswer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(keyboardRows.isEmpty() ? List.of() : keyboardRows.stream()
                                .map(InlineKeyboardRow::new)
                                .toList())
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }
}
