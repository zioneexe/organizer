package bot.tg.service;

import bot.tg.dto.TelegramContext;
import bot.tg.helper.TelegramHelper;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Response.REMINDER_DATE;
import static bot.tg.constant.ResponseMessage.ALRIGHT;
import static bot.tg.helper.ReminderResponseHelper.formDateChoiceKeyboard;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final TelegramClient telegramClient;
    private final TimeZoneService timeZoneService;

    public void startReminderCreation(UserRequest userRequest) {
        TelegramContext context = userRequest.getContext();
        UserSession userSession = userRequest.getUserSession();

        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, ALRIGHT);
        TelegramHelper.sendMessageWithMarkup(
                telegramClient, context.userId, REMINDER_DATE,
                formDateChoiceKeyboard(timeZoneService, context.userId)
        );


        userSession.createReminderDraft();
        userSession.setState(UserState.AWAITING_REMINDER_DATE);
    }

}
