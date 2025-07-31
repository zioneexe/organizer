package bot.tg.util;

import bot.tg.dto.Pageable;
import bot.tg.model.Reminder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static bot.tg.constant.Reminder.Callback.*;
import static bot.tg.constant.Reminder.Response.REMINDER_CREATE;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.util.TextHelper.escapeMarkdown;

public class ReminderMessageHelper {

    private ReminderMessageHelper() {}

    public static Map.Entry<List<List<InlineKeyboardButton>>, String> formRemindersMessage(List<Reminder> reminders,
                                                                                           Pageable pageable,
                                                                                           ZoneId userZoneId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> actionButtons = new ArrayList<>();

        if (reminders.isEmpty()) {
            actionButtons.add(formNewReminderButton());
            keyboardRows.add(actionButtons);

            return Map.entry(keyboardRows, "🔔 Немає запланованих нагадувань.");
        }

        int currentPage = pageable.getPage();
        int totalPages = pageable.getTotalPages();

        ZonedDateTime currentZonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(userZoneId);
        DateTimeFormatter currentDateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu, HH:mm z");

        StringBuilder answerBuilder = new StringBuilder("*Поточний час: ");
        answerBuilder.append(currentDateTimeFormatter.format(currentZonedDateTime)).append(" *\n\n");
        answerBuilder.append("*🔔 Ваші нагадування:*\n\n");
        Map<LocalDate, List<Reminder>> grouped = reminders.stream()
                .sorted(Comparator.comparing(Reminder::getDateTime))
                .collect(Collectors.groupingBy(reminder -> reminder.getDateTime()
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
                boolean isFired = reminder.getFired();
                boolean isEnabled = reminder.getEnabled();
                String firedEmoji = isFired ? "✅" : "⏰";
                String enabledEmoji = isEnabled ? " \uD83D\uDD15" : " \uD83D\uDD14";
                String enabledCallbackData = isEnabled ? DISABLE_REMINDER : ENABLE_REMINDER;
                String disabledMarkerString = isEnabled ? "" : " \uD83D\uDD15";

                ZonedDateTime zonedDateTime = reminder.getDateTime()
                        .atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(userZoneId);
                String time = zonedDateTime.format(timeFormatter);

                answerBuilder.append(index)
                        .append(".")
                        .append("   `").append(escapeMarkdown(reminder.getText())).append("`   ")
                        .append("\\[").append(time).append("] ").append(firedEmoji).append(disabledMarkerString)
                        .append("\n");

                actionButtons.add(InlineKeyboardButton.builder()
                        .text(index + enabledEmoji)
                        .callbackData(enabledCallbackData + COLON_DELIMITER + reminder.getId())
                        .build());

                actionButtons.add(InlineKeyboardButton.builder()
                        .text(index + " 🗑")
                        .callbackData(DELETE_REMINDER + COLON_DELIMITER + reminder.getId())
                        .build());

            }

            answerBuilder.append("\n");
        }

        answerBuilder.append("Сторінка ").append(currentPage).append(" / ").append(totalPages);

        List<InlineKeyboardButton> paginationButtons = new ArrayList<>();
        paginationButtons.add(InlineKeyboardButton.builder()
                .text("<")
                .callbackData(PAGE_REMINDER + COLON_DELIMITER + Math.max(currentPage - 1, 1))
                .build());
        paginationButtons.add(formNewReminderButton());
        paginationButtons.add(InlineKeyboardButton.builder()
                .text(">")
                .callbackData(PAGE_REMINDER + COLON_DELIMITER + Math.min(currentPage + 1, totalPages))
                .build());

        keyboardRows.add(actionButtons);
        keyboardRows.add(paginationButtons);

        return Map.entry(keyboardRows, answerBuilder.toString());
    }

    public static InlineKeyboardButton formNewReminderButton() {
        return InlineKeyboardButton.builder()
                .text(REMINDER_CREATE)
                .callbackData(NEW_REMINDER)
                .build();
    }

}
