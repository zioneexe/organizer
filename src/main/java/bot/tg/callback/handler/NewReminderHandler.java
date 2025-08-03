package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Callback.NEW_REMINDER;

@Component
@RequiredArgsConstructor
public class NewReminderHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final ReminderService reminderService;

    @Override
    public boolean supports(String data) {
        return data.equals(NEW_REMINDER);
    }

    @Override
    public void handle(Update update) {
        String callbackQueryId = update.getCallbackQuery().getId();

        this.reminderService.startReminderCreation(update);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }

}
