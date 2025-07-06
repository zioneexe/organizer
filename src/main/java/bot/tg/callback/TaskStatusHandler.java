package bot.tg.callback;

import bot.tg.dto.update.TaskUpdateDto;
import bot.tg.model.TaskStatus;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.util.Constants.*;

public class TaskStatusHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    public TaskStatusHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getTaskRepository();
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
        String[] parts = data.split(COLON_DELIMITER);

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

        String answerText = updatedStatus == TaskStatus.COMPLETED ? TASK_COMPLETED : TASK_IN_PROGRESS;
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(answerText)
                .showAlert(true)
                .build();

        EditMessageText editMessage = TasksResponseHelper.createTasksEditMessage(taskRepository, update);

        TelegramHelper.safeExecute(telegramClient, answer);
        TelegramHelper.safeExecute(telegramClient, editMessage);
    }
}
