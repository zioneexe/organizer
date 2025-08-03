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
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Callback.DELETE_REMINDER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class DeleteReminderHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final GoogleCalendarService googleCalendarService;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DELETE_REMINDER + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "❌ Некоректний запит на видалення.");
            return;
        }

        String reminderId = parts[1];
        this.googleCalendarService.deleteCalendarEvent(context.userId, reminderId);
        boolean deleted = reminderRepository.deleteById(reminderId);

        String response = deleted
                ? "🗑 Нагадування видалено."
                : "⚠️ Нагадування не знайдено.";

        TelegramHelper.sendEditMessage(telegramClient, context.messageId, context.userId, response);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int currentPage = userStateManager.getCurrentReminderPage(context.userId);
        Pageable pageable = paginationService.formReminderPageableForUser(currentPage, context.userId, userZoneId);
        SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(
                userStateManager,
                userRepository,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, remindersMessage);

        userStateManager.setState(context.userId, UserState.IDLE);
    }
}
