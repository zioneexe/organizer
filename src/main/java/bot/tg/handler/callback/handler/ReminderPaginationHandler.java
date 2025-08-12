package bot.tg.handler.callback.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.ReminderService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Callback.PAGE_REMINDER;
import static bot.tg.constant.ResponseMessage.INCORRECT_REQUEST_PAGE;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

@Component
@RequiredArgsConstructor
public class ReminderPaginationHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final ReminderService reminderService;

    @Override
    public boolean supports(String data) {
        return data.startsWith(PAGE_REMINDER + COLON_DELIMITER);
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
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, INCORRECT_REQUEST_PAGE);
            return;
        }

        int neededPage = Integer.parseInt(parts[1]);
        reminderService.sendRemindersPageEdit(request, neededPage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);

        userSession.setIdleState();
    }
}
