package bot.tg.state;

import bot.tg.helper.TaskHelper;
import bot.tg.model.TodoTask;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

public class TaskSelectionHandler implements StateHandler {

    private final TaskRepository taskRepository;
    private final TelegramClient telegramClient;

    public TaskSelectionHandler() {
        this.taskRepository = RepositoryProvider.getInstance().getTaskRepository();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void handle(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();

        List<TodoTask> tasks = taskRepository.getForTodayByUserId(userId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> tasksMessage = TaskHelper.formTasksMessage(tasks);
        List<List<InlineKeyboardButton>> keyboardRows = tasksMessage.getKey();
        String answer = tasksMessage.getValue();

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(
                                new InlineKeyboardRow(keyboardRows.get(0)),
                                new InlineKeyboardRow(keyboardRows.get(1))
                        ))
                        .build())
                .parseMode("Markdown")
                .build();

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
