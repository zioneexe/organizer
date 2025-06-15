package bot.tg.callback;

import bot.tg.dto.update.TaskUpdateDto;
import bot.tg.helper.TaskHelper;
import bot.tg.model.TaskStatus;
import bot.tg.model.TodoTask;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

import static bot.tg.Constants.COMPLETED_TASK;
import static bot.tg.Constants.IN_PROGRESS_TASK;

public class TaskStatusHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    public TaskStatusHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getInstance().getTaskRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(IN_PROGRESS_TASK) || data.startsWith(COMPLETED_TASK);
    }

    @Override
    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();
        String[] parts = data.split(":");

        String status = parts[0];
        String taskId = parts[1];

        if (!taskRepository.existsById(taskId)) {
            return;
        }

        TaskStatus updatedStatus = TaskStatus.fromString(status);
        TaskUpdateDto dto = TaskUpdateDto.builder()
                .status(updatedStatus)
                .build();

        taskRepository.update(taskId, dto);

        String answerText = updatedStatus == TaskStatus.COMPLETED ? "Завдання виконано. Вітаю!" : "Що ж.. Працюй далі :)";
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(answerText)
                .showAlert(true)
                .build();

        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        List<TodoTask> updatedTasks = taskRepository.getForTodayByUserId(userId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> updatedTasksMessage = TaskHelper.formTasksMessage(updatedTasks);
        List<List<InlineKeyboardButton>> keyboardRows = updatedTasksMessage.getKey();
        String editAnswer = updatedTasksMessage.getValue();

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(editAnswer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new InlineKeyboardRow(keyboardRows.get(0)),
                                new InlineKeyboardRow(keyboardRows.get(1))
                        ))
                        .build())
                .build();

        try {
            telegramClient.execute(answer);
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
