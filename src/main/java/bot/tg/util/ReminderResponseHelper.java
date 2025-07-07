package bot.tg.util;

import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class ReminderResponseHelper {

    private ReminderResponseHelper() {}

    public static SendMessage createRemindersMessage(UserRepository userRepository,
                                                     ReminderRepository reminderRepository,
                                                     Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        List<Reminder> reminders = reminderRepository.getUpcomingForUser(userId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> remindersMessage = ReminderHelper.formRemindersMessage(
                reminders, userZoneId
        );

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
