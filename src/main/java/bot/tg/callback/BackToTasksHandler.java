package bot.tg.callback;

import bot.tg.helper.TaskHelper;
import bot.tg.model.TodoTask;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

import static bot.tg.Constants.BACK_TO_TASKS;

public class BackToTasksHandler implements CallbackHandler {

    private final TaskRepository taskRepository;
    private final TelegramClient telegramClient;

    public BackToTasksHandler() {
        this.taskRepository = RepositoryProvider.getInstance().getTaskRepository();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public boolean supports(String data) {
        return data.equals(BACK_TO_TASKS);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String messageId = update.getCallbackQuery().getMessage().getMessageId().toString();

        List<TodoTask> tasks = taskRepository.getForTodayByUserId(userId);

        Map.Entry<List<List<InlineKeyboardButton>>, String> result = TaskHelper.formTasksMessage(tasks);

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(Integer.parseInt(messageId))
                .text(result.getValue())
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(List.of(
                        new InlineKeyboardRow(result.getKey().get(0)),
                        new InlineKeyboardRow(result.getKey().get(1))
                )).build())
                .parseMode("Markdown")
                .build();

        try {
            telegramClient.execute(edit);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
