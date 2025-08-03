package bot.tg.handler.callback.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.ReminderResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Callback.PAGE_REMINDER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class ReminderPaginationHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final PaginationService paginationService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(PAGE_REMINDER + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, "❌ Некоректний запит на зміну сторінки.");
            return;
        }

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        int neededPage = Integer.parseInt(parts[1]);
        Pageable pageable = paginationService.formReminderPageableForUser(neededPage, context.userId, userZoneId);
        EditMessageText pageMessage = ReminderResponseHelper.createRemindersEditMessage(
                userStateManager,
                userRepository,
                reminderRepository,
                pageable,
                context
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
