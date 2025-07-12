package bot.tg.util;

import bot.tg.model.Reminder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static bot.tg.constant.Reminder.Callback.DELETE_REMINDER;
import static bot.tg.constant.Reminder.Callback.NEW_REMINDER;
import static bot.tg.constant.Reminder.Response.REMINDER_CREATE;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.util.TextHelper.escapeMarkdown;

public class ReminderMessageHelper {

    private ReminderMessageHelper() {}

    public static Map.Entry<List<List<InlineKeyboardButton>>, String> formRemindersMessage(List<Reminder> reminders, ZoneId userZoneId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> actionButtons = new ArrayList<>();

        if (reminders.isEmpty()) {
            actionButtons.add(formNewReminderButton());
            keyboardRows.add(actionButtons);

            return Map.entry(keyboardRows, "🔔 Немає запланованих нагадувань.");
        }

        StringBuilder answerBuilder = new StringBuilder("*🔔 Ваші нагадування:*\n\n");
        Map<LocalDate, List<Reminder>> grouped = reminders.stream()
                .sorted(Comparator.comparing(Reminder::getDateTime))
                .collect(Collectors.groupingBy(r -> r.getDateTime()
                        .atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(userZoneId)
                        .toLocalDate(),
                        LinkedHashMap::new, Collectors.toList())
                );

        AtomicInteger counter = new AtomicInteger(0);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Map.Entry<LocalDate, List<Reminder>> entry : grouped.entrySet()) {
            LocalDate date = entry.getKey();
            List<Reminder> dayReminders = entry.getValue();

            answerBuilder.append("📅  *").append(date.format(dateFormatter)).append("*\n\n");

            for (Reminder reminder : dayReminders) {
                int index = counter.incrementAndGet();
                boolean fired = reminder.getFired();
                String emoji = fired ? "✅" : "⏰";

                ZonedDateTime zonedDateTime = reminder.getDateTime()
                        .atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(userZoneId);
                String time = zonedDateTime.format(timeFormatter);

                answerBuilder.append(index)
                        .append(".")
                        .append("   `").append(escapeMarkdown(reminder.getText())).append("`   ")
                        .append("\\[").append(time).append("] ").append(emoji)
                        .append("\n");

                actionButtons.add(InlineKeyboardButton.builder()
                        .text(index + " 🗑")
                        .callbackData(DELETE_REMINDER + COLON_DELIMITER + reminder.getId())
                        .build());

            }

            answerBuilder.append("\n");
        }

        keyboardRows.add(actionButtons);
        keyboardRows.add(List.of(formNewReminderButton()));

        return Map.entry(keyboardRows, answerBuilder.toString());
    }

    public static InlineKeyboardButton formNewReminderButton() {
        return InlineKeyboardButton.builder()
                .text(REMINDER_CREATE)
                .callbackData(NEW_REMINDER)
                .build();
    }

}
