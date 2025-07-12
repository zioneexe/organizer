package bot.tg.util;

import bot.tg.dto.ChatContext;
import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static bot.tg.constant.Reminder.Callback.DATE_PICKER;
import static bot.tg.constant.Symbol.COLON_DELIMITER;

public class ReminderResponseHelper {

    private ReminderResponseHelper() {}

    public static SendMessage createRemindersMessage(UserRepository userRepository,
                                                     ReminderRepository reminderRepository,
                                                     ChatContext chatContext) {
        long userId = chatContext.getChatId();
        long chatId = chatContext.getChatId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        List<Reminder> reminders = reminderRepository.getUpcomingForUser(userId);
        Map.Entry<List<List<InlineKeyboardButton>>, String> remindersMessage = ReminderMessageHelper.formRemindersMessage(
                reminders, userZoneId
        );

        List<List<InlineKeyboardButton>> keyboardRows = remindersMessage.getKey();
        String answer = remindersMessage.getValue();

        return SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(keyboardRows.isEmpty() ? List.of() : keyboardRows.stream()
                                .map(InlineKeyboardRow::new)
                                .toList())
                        .build())
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

    public static InlineKeyboardMarkup formDateChoiceKeyboard() {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            String text = switch (i) {
                case 0 -> "Сьогодні";
                case 1 -> "Завтра";
                default -> date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("uk")));
            };
            String callbackData = DATE_PICKER + COLON_DELIMITER + date;

            InlineKeyboardButton dayButton = InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(callbackData)
                    .build();

            rows.add(new InlineKeyboardRow(dayButton));
        }

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
