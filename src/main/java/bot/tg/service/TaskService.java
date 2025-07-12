package bot.tg.service;

import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.ResponseMessage.ALRIGHT;
import static bot.tg.constant.Task.Response.TASK_TITLE;

public class TaskService {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    public TaskService() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    public void startTaskCreation(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        SendMessage removeKeyboard = SendMessage.builder()
                .chatId(chatId)
                .text(ALRIGHT)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
        TelegramHelper.safeExecute(telegramClient, removeKeyboard);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(TASK_TITLE)
                .replyMarkup(ForceReplyKeyboard.builder().forceReply(true).build())
                .build();
        TelegramHelper.safeExecute(telegramClient, message);

        userStateManager.createTaskDraft(userId);
        userStateManager.setState(userId, UserState.AWAITING_TASK_TITLE);
    }
}
