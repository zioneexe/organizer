package bot.tg.service;

import bot.tg.dto.TelegramContext;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Response.REMINDER_DATE;
import static bot.tg.constant.ResponseMessage.ALRIGHT;
import static bot.tg.helper.ReminderResponseHelper.formDateChoiceKeyboard;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final UserStateManager userStateManager;
    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    public void startReminderCreation(TelegramContext context) {
        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, ALRIGHT);
        TelegramHelper.sendMessageWithMarkup(
                telegramClient, context.userId, REMINDER_DATE,
                formDateChoiceKeyboard(userRepository, context.userId)
        );

        userStateManager.createReminderDraft(context.userId);
        userStateManager.setState(context.userId, UserState.AWAITING_REMINDER_DATE);
    }

}
