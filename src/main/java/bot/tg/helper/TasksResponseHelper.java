package bot.tg.helper;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserSession;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class TasksResponseHelper {

    private TasksResponseHelper() {}

    public static SendMessage createTasksMessage(UserSession userSession,
                                                 UserRepository userRepository,
                                                 TaskRepository taskRepository,
                                                 Pageable pageable,
                                                 Long userId,
                                                 LocalDate chosenDate) {
        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        userSession.setCurrentTaskPage(1);
        List<TodoTask> tasks = taskRepository.getByUserForDayPaged(userId, pageable, chosenDate, userZoneId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> tasksMessage = TaskMessageHelper.formTasksMessage(tasks, pageable);
        List<List<InlineKeyboardButton>> keyboardRows = tasksMessage.getKey();
        String answer = tasksMessage.getValue();

        return SendMessage.builder()
                .chatId(userId)
                .text(answer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(keyboardRows.isEmpty() ? List.of() : keyboardRows.stream()
                                .map(InlineKeyboardRow::new)
                                .toList())
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

    public static EditMessageText createTasksEditMessage(UserSession userSession,
                                                         UserRepository userRepository,
                                                         TaskRepository taskRepository,
                                                         Pageable pageable,
                                                         LocalDate chosenDate,
                                                         TelegramContext context) {
        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        userSession.setCurrentTaskPage(pageable.getPage());
        List<TodoTask> updatedTasks = taskRepository.getByUserForDayPaged(context.userId, pageable, chosenDate, userZoneId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> updatedTasksMessage = TaskMessageHelper.formTasksMessage(updatedTasks, pageable);
        List<List<InlineKeyboardButton>> keyboardRows = updatedTasksMessage.getKey();
        String editAnswer = updatedTasksMessage.getValue();

        return EditMessageText.builder()
                .chatId(context.userId)
                .messageId(context.messageId)
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
