package bot.tg.util;

import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.Map;

public class ReminderResponseHelper {

    private ReminderResponseHelper() {}

    public static SendMessage createRemindersMessage(ReminderRepository reminderRepository, Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();

        List<Reminder> reminders = reminderRepository.getUpcomingForUser(userId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> remindersMessage = ReminderHelper.formRemindersMessage(reminders);
        List<List<InlineKeyboardButton>> keyboardRows = remindersMessage.getKey();
        String answer = remindersMessage.getValue();

        return SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(keyboardRows.isEmpty() ? List.of() : List.of(
                                new InlineKeyboardRow(keyboardRows.getFirst())
                        ))
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }
}
