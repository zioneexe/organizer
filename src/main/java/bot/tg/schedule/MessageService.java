package bot.tg.schedule;

import bot.tg.model.Reminder;
import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bot.tg.logging.Logger.log;

public class MessageService {

    private final MessageScheduler messageScheduler;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public MessageService() {
        this.reminderRepository = RepositoryProvider.getReminderRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.messageScheduler = new MessageScheduler();
    }

    public void scheduleReminder(Reminder reminder) {
        String userTimeZone = userRepository.getById(reminder.getUserId()).getTimeZone();
        log("Scheduling reminder for userId=" + reminder.getUserId() + ", timeZone='" + userTimeZone + "', reminderId=" + reminder.getId());

        if (!isSchedulable(reminder, userTimeZone)) {
            log("Reminder with id=" + reminder.getId() + " is not schedulable, skipping.");
            return;
        }
        messageScheduler.schedule(reminder);
        log("Reminder with id=" + reminder.getId() + " scheduled successfully.");
    }

    public void scheduleUnfiredReminders() {
        List<Reminder> reminders = reminderRepository.getUnfiredAfterNow();
        log("Found " + reminders.size() + " unfired reminders to schedule.");

        Set<Long> userIds = reminders.stream()
                .map(Reminder::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> usersById = userRepository.getByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        reminders.stream()
                .filter(reminder -> {
                    User user = usersById.get(reminder.getUserId());
                    String userTimeZone = user != null && user.getTimeZone() != null ? user.getTimeZone() : "";
                    boolean schedulable = isSchedulable(reminder, userTimeZone);
                    log("Reminder id=" + reminder.getId() + " for userId=" + reminder.getUserId() +
                            " with timezone='" + userTimeZone + "' schedulable=" + schedulable);
                    return schedulable;
                })
                .forEach(reminder -> {
                    messageScheduler.schedule(reminder);
                    log("Scheduled reminder with id=" + reminder.getId() + " for userId=" + reminder.getUserId());
                });
    }

    private boolean isSchedulable(Reminder reminder, String userTimeZone) {
        LocalDateTime localDateTime = reminder.getDateTime();
        Boolean fired = reminder.getFired();

        if (localDateTime == null || fired) {
            log("Reminder id=" + reminder.getId() + " is not schedulable because localDateTime is null or fired=" + fired);
            return false;
        }

        ZoneId zoneId = userTimeZone != null && !userTimeZone.isBlank()
                ? ZoneId.of(userTimeZone)
                : ZoneId.systemDefault();

        ZonedDateTime userZonedDateTime = localDateTime.atZone(zoneId);
        Instant reminderInstant = userZonedDateTime.toInstant();
        Instant nowInstant = Instant.now();

        boolean result = reminderInstant.isAfter(nowInstant);
        log("Checking schedulability for reminder id=" + reminder.getId() +
                ": reminderInstant=" + reminderInstant + ", nowInstant=" + nowInstant + ", result=" + result);

        return result;
    }

}
