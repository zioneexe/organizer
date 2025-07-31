package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.ChatContext;
import bot.tg.dto.Time;
import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.MenuHelper;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimePickerResponseHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Greetings.Callback.*;
import static bot.tg.constant.Greetings.Response.GREETING_TIME_SET;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class GreetingTimePickerHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final UserRepository userRepository;

    public GreetingTimePickerHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.messageService = ServiceProvider.getMessageService();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(GREETING_TIME_PICKER + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) return;

        String action = parts[1];

        Time time = userStateManager.getMorningGreetingTimeDraft(userId);
        switch (action) {
            case GREETING_CHANGE_HOUR -> {
                int delta = Integer.parseInt(parts[2]);
                int newHour = (time.getHour() + delta + 24) % 24;

                time.setHour(newHour);
                time.setTimeManuallyEdited(true);
            }
            case GREETING_CHANGE_MINUTE -> {
                int previousHour = time.getHour();
                int delta = Integer.parseInt(parts[2]);

                int minuteSum = time.getMinute() + delta;
                int newMinute = (minuteSum % 60 + 60) % 60;
                int deltaHour = Math.floorDiv(minuteSum, 60);

                int newHour = (previousHour + deltaHour + 24) % 24;

                time.setHour(newHour);
                time.setMinute(newMinute);
                time.setTimeManuallyEdited(true);
            }
            case GREETING_CANCEL -> {
                userStateManager.setState(userId, UserState.IDLE);
                TelegramHelper.sendEditMessage(telegramClient, messageId, chatId, "Зміна часу скасована.");
                SendMessage menuMessage = MenuHelper.formMenuMessage(new ChatContext(userId, userId));
                TelegramHelper.safeExecute(telegramClient, menuMessage);
                return;
            }
            case GREETING_CONFIRM -> {
                User user = userRepository.getById(userId);
                userRepository.setPreferredGreetingTime(userId, time);
                messageService.cancelGreetingForUser(user);
                messageService.scheduleGreetingForUser(user);

                ServiceProvider.getUserStateManager().setState(userId, UserState.IDLE);
                TelegramHelper.sendSimpleMessage(telegramClient, chatId, GREETING_TIME_SET + time + ".");
                TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
                return;
            }
        }

        EditMessageText editMessage = TimePickerResponseHelper.createGreetingTimePickerEditMessage(update);
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
