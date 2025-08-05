package bot.tg.handler.callback.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.ReminderResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Callback.DISABLE_REMINDER;
import static bot.tg.constant.Reminder.Callback.ENABLE_REMINDER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class ReminderToggleHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final MessageService messageService;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(DISABLE_REMINDER + COLON_DELIMITER)
                || data.startsWith(ENABLE_REMINDER + COLON_DELIMITER);
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
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "❌ Некоректний запит на видалення.");
            return;
        }

        String reminderId = parts[1];
        Reminder reminder = reminderRepository.getById(reminderId);

        String response = "";
        if (context.data.startsWith(DISABLE_REMINDER + COLON_DELIMITER)) {
            reminderRepository.setEnabled(reminderId, false);
            messageService.cancelReminder(reminder);
            response = "\uD83D\uDD15 Нагадування вимкнено.";
        } else if (context.data.startsWith(ENABLE_REMINDER + COLON_DELIMITER)) {
            reminderRepository.setEnabled(reminderId, true);
            messageService.scheduleReminder(reminder);
            response = "\uD83D\uDD14 Нагадування увімкнено.";
        }

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
                userRepository,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, remindersMessage);
    }
}
