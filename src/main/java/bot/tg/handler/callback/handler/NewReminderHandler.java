package bot.tg.handler.callback.handler;

import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.ReminderService;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Callback.NEW_REMINDER;

@Component
@RequiredArgsConstructor
public class NewReminderHandler extends CallbackHandler {

    private final TelegramClient telegramClient;
    private final ReminderService reminderService;

    @Override
    public boolean supports(String data) {
        return data.equals(NEW_REMINDER);
    }

    @Override
    public void handle(UserRequest request) {
        String callbackQueryId = request.getUpdate().getCallbackQuery().getId();

        this.reminderService.startReminderCreation(request.getContext());
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
