package bot.tg.service;

import bot.tg.model.Reminder;
import bot.tg.model.User;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.schedule.MessageScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageScheduler messageScheduler;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public void scheduleReminder(Reminder reminder) {
        Stream.of(reminder)
                .filter(this::isSchedulable)
                .forEach(messageScheduler::scheduleReminder);
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
            if (!user.getGreetingsEnabled()) {
                log.debug("Daily greetings are disabled for user with id={}", user.getUserId());
                continue;
            }
            messageScheduler.scheduleGreetingForUser(user);
            log.info("Scheduling daily greeting for user with id={}", user.getUserId());
        }
    }

    private boolean isSchedulable(Reminder reminder) {
        if (!reminder.getEnabled()) return false;

        LocalDateTime utcDateTime = reminder.getDateTime();
        Boolean fired = reminder.getFired();

        if (utcDateTime == null || fired) {
            log.error("Reminder id={} is not schedulable because utcDateTime is null or fired={}", reminder.getId(), fired);
            return false;
        }

        Instant reminderInstant = utcDateTime.atZone(ZoneOffset.UTC).toInstant();
        Instant nowInstant = Instant.now();

        boolean result = reminderInstant.isAfter(nowInstant);
        log.info("Checking schedulability for reminder id={}: reminderInstant={}, nowInstant={}, result={}", reminder.getId(), reminderInstant, nowInstant, result);

        return result;
    }

}
