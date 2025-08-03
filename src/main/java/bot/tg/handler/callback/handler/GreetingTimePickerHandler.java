package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.Time;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimePickerResponseHelper;
import bot.tg.model.User;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Greetings.Callback.*;
import static bot.tg.constant.Greetings.Response.GREETING_TIME_SET;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class GreetingTimePickerHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final UserRepository userRepository;

    @Override
    public boolean supports(String data) {
        return data.startsWith(GREETING_TIME_PICKER + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) return;

        String action = parts[1];

        Time time = userStateManager.getMorningGreetingTimeDraft(context.userId);
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
                userStateManager.setState(context.userId, UserState.IDLE);
                TelegramHelper.sendEditMessage(telegramClient, context.messageId, context.userId, "Зміна часу скасована.");
                SendMessage menuMessage = MenuHelper.formMenuMessage(context.userId);
                TelegramHelper.safeExecute(telegramClient, menuMessage);
                return;
            }
            case GREETING_CONFIRM -> {
                User user = userRepository.getById(context.userId);
                userRepository.setPreferredGreetingTime(context.userId, time);
                messageService.cancelGreetingForUser(user);
                messageService.scheduleGreetingForUser(user);

                userStateManager.setState(context.userId, UserState.IDLE);
                TelegramHelper.sendSimpleMessage(telegramClient, context.userId, GREETING_TIME_SET + time + ".");
                TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
                return;
            }
        }

        EditMessageText editMessage = TimePickerResponseHelper.createGreetingTimePickerEditMessage(context, userStateManager);
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
