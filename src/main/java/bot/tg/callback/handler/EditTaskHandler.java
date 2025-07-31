package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.ChatContext;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.MenuHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.*;

public class EditTaskHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    public EditTaskHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(EDIT_TASK + COLON_DELIMITER)
                || data.startsWith(EDIT_NAME_TASK + COLON_DELIMITER)
                || data.startsWith(EDIT_DESCRIPTION_TASK + COLON_DELIMITER)
                || data.equals(CANCEL_EDIT_TASK);
    }

    @Override
    public void handle(Update update) {
        String action = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        long userId = update.getCallbackQuery().getFrom().getId();
        String callbackQueryId = update.getCallbackQuery().getId();

        if (action.startsWith(EDIT_TASK + COLON_DELIMITER)) {
            String taskId = action.split(COLON_DELIMITER)[1];

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("Змінити назву")
                                    .callbackData(EDIT_NAME_TASK + COLON_DELIMITER + taskId)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("Змінити опис")
                                    .callbackData(EDIT_DESCRIPTION_TASK + COLON_DELIMITER + taskId)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("🗑 Видалити")
                                    .callbackData(DELETE_TASK + COLON_DELIMITER + taskId)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("❎ Скасувати")
                                    .callbackData(CANCEL_EDIT_TASK)
                                    .build()
                    ))
                    .build();

            TelegramHelper.sendEditMessageWithMarkup(telegramClient, messageId, chatId, "Що хочеш змінити?", keyboard);
            return;
        }

        if (action.startsWith(EDIT_NAME_TASK + COLON_DELIMITER)) {
            String taskId = action.split(COLON_DELIMITER)[1];

            userStateManager.setState(userId, UserState.EDITING_TASK_NAME);
            userStateManager.setEditingTaskId(userId, taskId);

            TelegramHelper.sendMessageWithForceReply(telegramClient, userId, "Введи нову назву.");
            return;
        }

        if (action.startsWith(EDIT_DESCRIPTION_TASK + COLON_DELIMITER)) {
            String taskId = action.split(COLON_DELIMITER)[1];

            userStateManager.setState(userId, UserState.EDITING_TASK_DESCRIPTION);
            userStateManager.setEditingTaskId(userId, taskId);

            TelegramHelper.sendMessageWithForceReply(telegramClient, userId, "Введи новий опис.");
            return;
        }

        if (action.equals(CANCEL_EDIT_TASK)) {
            userStateManager.setState(userId, UserState.IDLE);
            userStateManager.clearEditingTaskId(userId);

            TelegramHelper.sendEditMessage(telegramClient, messageId, chatId, "Редагування скасовано.");
            SendMessage menuMessage = MenuHelper.formMenuMessage(new ChatContext(userId, userId));
            TelegramHelper.safeExecute(telegramClient, menuMessage);
        }

        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }


}
