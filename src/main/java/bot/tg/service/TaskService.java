package bot.tg.service;

import bot.tg.dto.ChatContext;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
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
        ChatContext chatContext = TelegramHelper.extractChatContext(update);
        if (chatContext == null) return;
        long chatId = chatContext.getChatId();
        long userId = chatContext.getUserId();

        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, chatId, ALRIGHT);
        TelegramHelper.sendMessageWithForceReply(telegramClient, chatId, TASK_TITLE);

        userStateManager.createTaskDraft(userId);
        userStateManager.setState(userId, UserState.AWAITING_TASK_TITLE);
    }
}
