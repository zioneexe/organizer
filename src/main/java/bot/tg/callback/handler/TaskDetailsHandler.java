package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.model.TodoTask;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.util.TaskMessageHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.DETAILS_TASK;

public class TaskDetailsHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    public TaskDetailsHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.taskRepository = RepositoryProvider.getTaskRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(DETAILS_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String taskId = data.split(COLON_DELIMITER)[1];
        if (!taskRepository.existsById(taskId)) {
            return;
        }

        TodoTask task = taskRepository.getById(taskId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> details = TaskMessageHelper.formDetailsMessage(task);
        List<InlineKeyboardButton> buttons = details.getKey().getFirst();

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(details.getValue())
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(new InlineKeyboardRow(buttons)))
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
