package bot.tg.service;

import bot.tg.model.Reminder;
import bot.tg.model.User;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.schedule.MessageScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageScheduler messageScheduler;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final TimeZoneService timeZoneService;

    public void scheduleReminder(Reminder reminder) {
        if (!isSchedulable(reminder)) return;
        messageScheduler.scheduleReminder(reminder);
    }

    public void scheduleUnfiredReminders() {
        List<Reminder> reminders = reminderRepository.getUnfiredAfterNow();
        log.info("Found {} unfired reminders to schedule.", reminders.size());

        reminders.stream()
                .filter(reminder -> {
                    boolean schedulable = isSchedulable(reminder);
                    log.info("Reminder id={} for userId={} schedulable={}", reminder.getId(), reminder.getUserId(), schedulable);
                    return schedulable;
                })
                .forEach(reminder -> {
                    messageScheduler.scheduleReminder(reminder);
                    log.info("Scheduled reminder with id={} for userId={}", reminder.getId(), reminder.getUserId());
                });
    }

    public void cancelReminder(Reminder reminder) {
        messageScheduler.cancelReminder(reminder);
    }

    public void scheduleGreetingForUser(User user) {
        messageScheduler.scheduleGreetingForUser(user);
    }

    public void cancelGreetingForUser(User user) {
        messageScheduler.cancelGreetingForUser(user);
    }

    public void scheduleGreetingsToAll() {
        List<User> users = userRepository.getAll();

        for (User user : users) {
            if (!user.getGreetingsEnabled()) continue;
            messageScheduler.scheduleGreetingForUser(user);
        }
    }

    private boolean isSchedulable(Reminder reminder) {
        if (!reminder.getEnabled()) return false;

        LocalDateTime localDateTime = reminder.getDateTime();
        Boolean fired = reminder.getFired();

        if (localDateTime == null || fired) {
            log.error("Reminder id={} is not schedulable because localDateTime is null or fired={}", reminder.getId(), fired);
            return false;
        }

        ZoneId userZoneId = timeZoneService.getUserZoneId(reminder.getUserId());
        ZonedDateTime systemZonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        ZonedDateTime userZonedDateTime = systemZonedDateTime.withZoneSameInstant(userZoneId);
        Instant reminderInstant = userZonedDateTime.toInstant();
        Instant nowInstant = Instant.now();

        boolean result = reminderInstant.isAfter(nowInstant);
        log.info("Checking schedulability for reminder id={}: reminderInstant={}, nowInstant={}, result={}", reminder.getId(), reminderInstant, nowInstant, result);

        return result;
    }

}
