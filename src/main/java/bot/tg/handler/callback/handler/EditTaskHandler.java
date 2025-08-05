package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.*;

@Component
@RequiredArgsConstructor
public class EditTaskHandler extends CallbackHandler {

    private final TelegramClient telegramClient;

    @Override
    public boolean supports(String data) {
        return data.startsWith(EDIT_TASK + COLON_DELIMITER)
                || data.startsWith(EDIT_NAME_TASK + COLON_DELIMITER)
                || data.startsWith(EDIT_DESCRIPTION_TASK + COLON_DELIMITER)
                || data.equals(CANCEL_EDIT_TASK);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.data == null) {
            return;
        }

        if (context.data.startsWith(EDIT_TASK + COLON_DELIMITER)) {
            String taskId = context.data.split(COLON_DELIMITER)[1];

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("Назву")
                                    .callbackData(EDIT_NAME_TASK + COLON_DELIMITER + taskId)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("Опис")
                                    .callbackData(EDIT_DESCRIPTION_TASK + COLON_DELIMITER + taskId)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("Видалити")
                                    .callbackData(DELETE_TASK + COLON_DELIMITER + taskId)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("Скасувати")
                                    .callbackData(CANCEL_EDIT_TASK)
                                    .build()
                    ))
                    .build();

            TelegramHelper.sendEditMessageWithMarkup(telegramClient, context.messageId, context.chatId, "Що хочеш змінити?", keyboard);
            return;
        }

        if (context.data.startsWith(EDIT_NAME_TASK + COLON_DELIMITER)) {
            String taskId = context.data.split(COLON_DELIMITER)[1];

            userSession.setState(UserState.EDITING_TASK_NAME);
            userSession.setEditingTaskId(taskId);

            TelegramHelper.sendMessageWithForceReply(telegramClient, context.userId, "Введи нову назву.");
            return;
        }

        if (context.data.startsWith(EDIT_DESCRIPTION_TASK + COLON_DELIMITER)) {
            String taskId = context.data.split(COLON_DELIMITER)[1];

            userSession.setState(UserState.EDITING_TASK_DESCRIPTION);
            userSession.setEditingTaskId(taskId);

            TelegramHelper.sendMessageWithForceReply(telegramClient, context.userId, "Введи новий опис.");
            return;
        }

        if (context.data.equals(CANCEL_EDIT_TASK)) {
            userSession.setIdleState();
            userSession.clearEditingTaskId();

            TelegramHelper.sendEditMessage(telegramClient, context.messageId, context.userId, "Редагування скасовано.");
            SendMessage menuMessage = MenuHelper.formMenuMessage(context.userId);
            TelegramHelper.safeExecute(telegramClient, menuMessage);
        }

        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }


}
