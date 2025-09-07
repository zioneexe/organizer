package bot.tg.helper;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import bot.tg.service.TimeZoneService;
import bot.tg.user.UserSession;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static bot.tg.constant.Core.Pagination.*;
import static bot.tg.constant.Reminder.Callback.*;
import static bot.tg.constant.Reminder.Response.*;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.util.MarkupAdjuster.escapeMarkdown;

public class ReminderHelper {

    private ReminderHelper() {
    }

    public static Map.Entry<InlineKeyboardMarkup, String> formRemindersMessage(List<Reminder> reminders,
                                                                                           Pageable pageable,
                                                                                           ZoneId userZoneId) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> actionButtons = new ArrayList<>();

        if (reminders.isEmpty()) {
            actionButtons.add(formNewReminderButton());
            keyboardRows.add(new InlineKeyboardRow(actionButtons));

            return Map.entry(
                    InlineKeyboardMarkup.builder().keyboard(keyboardRows).build(),
                    REMINDER_NO_PLANNED
            );
        }

        int currentPage = pageable.getPage();
        int totalPages = pageable.getTotalPages();

        ZonedDateTime currentZonedDateTime = ZonedDateTime.now(userZoneId);
        DateTimeFormatter currentDateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu, HH:mm z");

        StringBuilder answerBuilder = new StringBuilder();
        answerBuilder.append("*")
                .append(REMINDER_CURRENT_TIME)
                .append(currentDateTimeFormatter.format(currentZonedDateTime))
                .append(" *\n\n*")
                .append(REMINDER_YOUR_REMINDERS)
                .append("*\n\n");

        Map<LocalDate, List<Reminder>> grouped = reminders.stream()
                .sorted(Comparator.comparing(Reminder::getDateTime))
                .collect(Collectors.groupingBy(reminder -> reminder.getDateTime()
                        .atZone(ZoneOffset.UTC)
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
                        .atZone(ZoneOffset.UTC)
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

        answerBuilder.append(PAGINATION_PAGE).append(currentPage).append(PAGINATION_DIVIDER).append(totalPages);

        List<InlineKeyboardButton> paginationButtons = new ArrayList<>();
        paginationButtons.add(InlineKeyboardButton.builder()
                .text(PAGINATION_PREV)
                .callbackData(PAGE_REMINDER + COLON_DELIMITER + Math.max(currentPage - 1, 1))
                .build());
        paginationButtons.add(formNewReminderButton());
        paginationButtons.add(InlineKeyboardButton.builder()
                .text(PAGINATION_NEXT)
                .callbackData(PAGE_REMINDER + COLON_DELIMITER + Math.min(currentPage + 1, totalPages))
                .build());

        keyboardRows.add(new InlineKeyboardRow(actionButtons));
        keyboardRows.add(new InlineKeyboardRow(paginationButtons));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();

        return Map.entry(keyboard, answerBuilder.toString());
    }

    public static InlineKeyboardButton formNewReminderButton() {
        return InlineKeyboardButton.builder()
                .text(REMINDER_CREATE)
                .callbackData(NEW_REMINDER)
                .build();
    }


    public static SendMessage createRemindersMessage(UserSession userSession,
                                                     TimeZoneService timeZoneService,
                                                     ReminderRepository reminderRepository,
                                                     Pageable pageable,
                                                     Long userId) {
        ZoneId userZoneId = timeZoneService.getUserZoneId(userId);

        userSession.setCurrentReminderPage(1);
        List<Reminder> reminders = reminderRepository.getUpcomingForUserPaged(userId, pageable, userZoneId);
        Map.Entry<InlineKeyboardMarkup, String> remindersMessage = ReminderHelper.formRemindersMessage(
                reminders, pageable, userZoneId
        );

        InlineKeyboardMarkup keyboard = remindersMessage.getKey();
        String answer = remindersMessage.getValue();

        return SendMessage.builder()
                .chatId(userId)
                .text(answer)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

    public static EditMessageText createRemindersEditMessage(UserSession userSession,
                                                             TimeZoneService timeZoneService,
                                                             ReminderRepository reminderRepository,
                                                             Pageable pageable,
                                                             TelegramContext context) {
        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);

        userSession.setCurrentReminderPage(pageable.getPage());
        List<Reminder> updatedReminders = reminderRepository.getUpcomingForUserPaged(context.userId, pageable, userZoneId);
        Map.Entry<InlineKeyboardMarkup, String> updatedRemindersMessage = ReminderHelper.formRemindersMessage(
                updatedReminders, pageable, userZoneId
        );

        InlineKeyboardMarkup keyboard = updatedRemindersMessage.getKey();
        String editAnswer = updatedRemindersMessage.getValue();

        return EditMessageText.builder()
                .chatId(context.userId)
                .messageId(context.messageId)
                .text(editAnswer)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

    public static InlineKeyboardMarkup formDateChoiceKeyboard(TimeZoneService timeZoneService, Long userId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        ZoneId userZoneId = timeZoneService.getUserZoneId(userId);

        for (int i = 0; i < 8; i++) {
            LocalDate date = LocalDate.now(userZoneId).plusDays(i);
            String text = switch (i) {
                case 0 -> REMINDER_TODAY;
                case 1 -> REMINDER_TOMORROW;
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
