package bot.tg.service;

import bot.tg.model.Reminder;
import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.schedule.MessageScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MessageService {

    private final MessageScheduler messageScheduler;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    public MessageService() {
        this.reminderRepository = RepositoryProvider.getReminderRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.messageScheduler = new MessageScheduler();
    }

    public void scheduleReminder(Reminder reminder) {
        String userTimeZone = userRepository.getById(reminder.getUserId()).getTimeZone();

        if (!isSchedulable(reminder, userTimeZone)) {
            return;
        }
        messageScheduler.schedule(reminder);
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
                    messageScheduler.schedule(reminder);
                    log.info("Scheduled reminder with id={} for userId={}", reminder.getId(), reminder.getUserId());
                });
    }

    public void scheduleGoodMorningForUser(User user) {
        messageScheduler.scheduleGoodMorningForUser(user);
    }

    public void unscheduleGoodMorningForUser(User user) {
        messageScheduler.unscheduleGoodMorningForUser(user);
    }

    public void scheduleGoodMorningToAll() {
        List<User> users = userRepository.getAll();

        for (User user : users) {
            if (!user.getMorningGreetingsEnabled()) continue;
            messageScheduler.scheduleGoodMorningForUser(user);
        }
    }

    private boolean isSchedulable(Reminder reminder, String userTimeZone) {
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
