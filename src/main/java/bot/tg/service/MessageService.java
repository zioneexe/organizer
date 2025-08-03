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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageScheduler messageScheduler;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public void scheduleReminder(Reminder reminder) {
        String userTimeZone = userRepository.getById(reminder.getUserId()).getTimeZone();

        if (!isSchedulable(reminder, userTimeZone)) {
            return;
        }
        messageScheduler.scheduleReminder(reminder);
    }

    public void scheduleUnfiredReminders() {
        List<Reminder> reminders = reminderRepository.getUnfiredAfterNow();
        log.info("Found {} unfired reminders to schedule.", reminders.size());

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
                    log.info("Reminder id={} for userId={} with timezone='{}' schedulable={}", reminder.getId(), reminder.getUserId(), userTimeZone, schedulable);
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

    private boolean isSchedulable(Reminder reminder, String userTimeZone) {
        if (!reminder.getEnabled()) return false;

        LocalDateTime localDateTime = reminder.getDateTime();
        Boolean fired = reminder.getFired();

        if (localDateTime == null || fired) {
            log.error("Reminder id={} is not schedulable because localDateTime is null or fired={}", reminder.getId(), fired);
            return false;
        }

        ZoneId zoneId = userTimeZone != null && !userTimeZone.isBlank()
                ? ZoneId.of(userTimeZone)
                : ZoneId.systemDefault();

        ZonedDateTime systemZonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        ZonedDateTime userZonedDateTime = systemZonedDateTime.withZoneSameInstant(zoneId);
        Instant reminderInstant = userZonedDateTime.toInstant();
        Instant nowInstant = Instant.now();

        boolean result = reminderInstant.isAfter(nowInstant);
        log.info("Checking schedulability for reminder id={}: reminderInstant={}, nowInstant={}, result={}", reminder.getId(), reminderInstant, nowInstant, result);

        return result;
    }

}
