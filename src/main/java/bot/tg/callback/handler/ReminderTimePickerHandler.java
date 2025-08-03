package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.ChatContext;
import bot.tg.dto.DateTime;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimePickerResponseHelper;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static bot.tg.constant.Reminder.Callback.*;
import static bot.tg.constant.Reminder.Response.REMINDER_TEXT;
import static bot.tg.constant.ResponseMessage.INVALID_TIME;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class ReminderTimePickerHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;

    @Override
    public boolean supports(String data) {
        return data.startsWith(REMINDER_TIME_PICKER + COLON_DELIMITER);
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
        ReminderCreateDto dto = userStateManager.getReminderDraft(userId);
        DateTime dateTime = dto.getDateTime();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId zoneId = userTimeZone != null && !userTimeZone.isBlank() ?
                ZoneId.of(userTimeZone) :
                ZoneId.systemDefault();

        boolean isToday = dateTime.getDate().isEqual(LocalDate.now(zoneId));
        int currentHour = LocalTime.now(zoneId).getHour();
        int currentMinute = LocalTime.now(zoneId).getMinute();

        boolean isValidAction = true;
        switch (action) {
            case REMINDER_CHANGE_HOUR -> {
                int delta = Integer.parseInt(parts[2]);
                int newHour = (dateTime.getHour() + delta + 24) % 24;

                if (isToday && newHour < currentHour) isValidAction = false;
                if (isValidAction) {
                    dateTime.setHour(newHour);
                    dateTime.setTimeManuallyEdited(true);
                }
            }
            case REMINDER_CHANGE_MINUTE -> {
                int previousHour = dateTime.getHour();
                int delta = Integer.parseInt(parts[2]);

                int minuteSum = dateTime.getMinute() + delta;
                int newMinute = (minuteSum % 60 + 60) % 60;
                int deltaHour = Math.floorDiv(minuteSum, 60);

                int newHour = (previousHour + deltaHour + 24) % 24;

                if (isToday && newHour < currentHour) isValidAction = false;
                if (isToday && newHour == currentHour && newMinute < currentMinute) isValidAction = false;

                if (isValidAction) {
                    dateTime.setHour(newHour);
                    dateTime.setMinute(newMinute);
                    dateTime.setTimeManuallyEdited(true);
                }
            }
            case REMINDER_CANCEL -> {
                userStateManager.setState(userId, UserState.IDLE);
                TelegramHelper.sendEditMessage(telegramClient, messageId, chatId, "Створення скасовано.");
                SendMessage menuMessage = MenuHelper.formMenuMessage(new ChatContext(userId, userId));
                TelegramHelper.safeExecute(telegramClient, menuMessage);
                return;
            }
            case REMINDER_CONFIRM -> {
                ZonedDateTime now = ZonedDateTime.now(zoneId).withSecond(0).withNano(0);
                ZonedDateTime currentlySetTime = DateTime.DateTimeMapper.toZonedDateTime(dateTime)
                        .withSecond(0).withNano(0);

                if (now.isAfter(currentlySetTime)) {
                    isValidAction = false;
                    break;
                }

                userStateManager.setState(userId, UserState.AWAITING_REMINDER_TEXT);
                TelegramHelper.sendMessageWithForceReply(telegramClient, chatId, REMINDER_TEXT);
                TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
                return;
            }
        }

        if (!isValidAction) {
            TelegramHelper.sendCallbackAnswerWithMessage(telegramClient, callbackQueryId, INVALID_TIME);
        }

        EditMessageText editMessage = TimePickerResponseHelper.createReminderTimePickerEditMessage(update, userTimeZone, userStateManager);
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
