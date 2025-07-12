package bot.tg.service;

import bot.tg.dto.ChatContext;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Response.REMINDER_DATE;
import static bot.tg.constant.ResponseMessage.ALRIGHT;
import static bot.tg.util.ReminderResponseHelper.formDateChoiceKeyboard;

public class ReminderService {

    private final UserStateManager userStateManager;
    private final TelegramClient telegramClient;

    public ReminderService() {
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    public void startReminderCreation(Update update) {
        ChatContext chatContext = TelegramHelper.extractChatContext(update);
        if (chatContext == null) return;
        long chatId = chatContext.getChatId();
        long userId = chatContext.getUserId();

        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, chatId, ALRIGHT);
        TelegramHelper.sendMessageWithMarkup(telegramClient, chatId, REMINDER_DATE, formDateChoiceKeyboard());

        userStateManager.createReminderDraft(userId);
        userStateManager.setState(userId, UserState.AWAITING_REMINDER_DATE);
    }

}
