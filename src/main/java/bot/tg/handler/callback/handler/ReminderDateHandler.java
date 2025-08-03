package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimePickerResponseHelper;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static bot.tg.constant.Reminder.Callback.DATE_PICKER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class ReminderDateHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DATE_PICKER + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.data == null) {
            return;
        }

        ReminderCreateDto dto = userStateManager.getReminderDraft(context.userId);
        String dateString = context.data.split(COLON_DELIMITER)[1];
        LocalDate date = parseDate(dateString);
        dto.getDateTime().setDate(date);
        userStateManager.setState(context.userId, UserState.AWAITING_REMINDER_TIME);

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();

        EditMessageText editMessage = TimePickerResponseHelper.createReminderTimePickerEditMessage(context, userTimeZone, userStateManager);
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }


    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
