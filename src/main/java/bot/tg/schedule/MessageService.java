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
        if (!isSchedulable(reminder, userTimeZone)) return;
        messageScheduler.schedule(reminder);
    }

    public void scheduleUnfiredReminders() {
        List<Reminder> reminders = reminderRepository.getUnfiredAfterNow();

        Set<Long> userIds = reminders.stream()
                .map(Reminder::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> usersById = userRepository.getByIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        reminders.stream()
                .filter(reminder -> {
                    User user = usersById.get(reminder.getUserId());
                    String userTimeZone = user != null && user.getTimeZone() != null ? user.getTimeZone() : "";
                    return isSchedulable(reminder, userTimeZone);
                })
                .forEach(messageScheduler::schedule);
    }

    public void scheduleGoodMorningToAll() {
        List<User> users = userRepository.getAll();

        for (User user : users) {
            messageScheduler.scheduleGoodMorningForUser(user);
        }
    }

    private boolean isSchedulable(Reminder reminder, String userTimeZone) {
        LocalDateTime localDateTime = reminder.getDateTime();
        Boolean fired = reminder.getFired();

        if (localDateTime == null || fired) {
            return false;
        }

        ZoneId zoneId = userTimeZone != null && !userTimeZone.isBlank()
                ? ZoneId.of(userTimeZone)
                : ZoneId.systemDefault();

        ZonedDateTime userZonedDateTime = localDateTime.atZone(zoneId);
        Instant reminderInstant = userZonedDateTime.toInstant();
        Instant nowInstant = Instant.now();

        return reminderInstant.isAfter(nowInstant);
    }

}
