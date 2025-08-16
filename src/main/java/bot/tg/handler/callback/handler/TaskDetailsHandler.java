package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TaskHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.TodoTask;
import bot.tg.repository.TaskRepository;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.DETAILS_TASK;

@Component
@RequiredArgsConstructor
public class TaskDetailsHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final TaskRepository taskRepository;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DETAILS_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.data == null) {
            return;
        }

        String taskId = context.data.split(COLON_DELIMITER)[1];
        if (!taskRepository.existsById(taskId)) {
            return;
        }

        TodoTask task = taskRepository.getById(taskId);
        Map.Entry<InlineKeyboardMarkup, String> details = TaskHelper.formDetailsMessage(task);
        InlineKeyboardMarkup keyboard = details.getKey();

        EditMessageText editMessage = EditMessageText.builder()
                .chatId(context.userId)
                .messageId(context.messageId)
                .text(details.getValue())
                .replyMarkup(keyboard)
                .parseMode(ParseMode.MARKDOWN)
                .build();
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
