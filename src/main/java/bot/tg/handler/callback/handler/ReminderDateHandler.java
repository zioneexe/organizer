package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimePickerResponseHelper;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import static bot.tg.constant.Reminder.Callback.DATE_PICKER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class ReminderDateHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DATE_PICKER + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.data == null) {
            return;
        }

        ReminderCreateDto dto = userSession.getReminderDraft();
        String dateString = context.data.split(COLON_DELIMITER)[1];
        LocalDate date = parseDate(dateString);
        dto.getDateTime().setDate(date);
        userSession.setState(UserState.AWAITING_REMINDER_TIME);

        ZoneId userTimeZone = ZoneId.of(userRepository.getById(context.userId).getTimeZone());
        EditMessageText editMessage = TimePickerResponseHelper.createReminderTimePickerEditMessage(context, userTimeZone, userSession);
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
