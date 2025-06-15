package bot.tg.callback;

import bot.tg.helper.TaskHelper;
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

import static bot.tg.Constants.DETAILS_TASK;

public class TaskDetailsHandler implements CallbackHandler{

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    public TaskDetailsHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getInstance().getTaskRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DETAILS_TASK);
    }

    @Override
    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String taskId = data.split(":")[1];
        if (!taskRepository.existsById(taskId)) {
            return;
        }

        TodoTask task = taskRepository.getById(taskId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> details = TaskHelper.formDetailsMessage(task);
        List<InlineKeyboardButton> buttons = details.getKey().getFirst();

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(details.getValue())
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(new InlineKeyboardRow(buttons)))
                        .build())
                .parseMode("Markdown")
                .build();

        try {
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();

        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
