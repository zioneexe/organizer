package bot.tg.handler.callback.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.ReminderResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleCalendarService;
import bot.tg.service.PaginationService;
import bot.tg.service.TimeZoneService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Callback.DELETE_REMINDER;
import static bot.tg.constant.Reminder.Response.REMINDER_DELETED;
import static bot.tg.constant.Reminder.Response.REMINDER_NOT_FOUND;
import static bot.tg.constant.ResponseMessage.INCORRECT_REQUEST_DELETE;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class DeleteReminderHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final GoogleCalendarService googleCalendarService;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;
    private final TimeZoneService timeZoneService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_REMINDER + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, INCORRECT_REQUEST_DELETE);
            return;
        }

        String reminderId = parts[1];
        this.googleCalendarService.deleteCalendarEvent(context.userId, reminderId);
        boolean deleted = reminderRepository.deleteById(reminderId);

        String response = deleted ? REMINDER_DELETED : REMINDER_NOT_FOUND;

        TelegramHelper.sendEditMessage(telegramClient, context.messageId, context.userId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userSession.getCurrentReminderPage();
        Pageable pageable = paginationService.formReminderPageableForUser(currentPage, context.userId, userZoneId);
        SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(
                userSession,
                timeZoneService,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, remindersMessage);

        userSession.setIdleState();
    }
}
