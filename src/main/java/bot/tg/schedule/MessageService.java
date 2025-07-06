package bot.tg.schedule;

import bot.tg.model.Reminder;
import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

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
        if (!isSchedulable(reminder)) return;
        messageScheduler.schedule(reminder);
    }

    public void scheduleUnfiredReminders() {
        reminderRepository.getUnfiredAfterNow().stream()
                .filter(this::isSchedulable)
                .forEach(messageScheduler::schedule);
    }

    public void scheduleGoodMorningToAll() {
        List<User> users = userRepository.getAll();

        for (User user : users) {
            messageScheduler.scheduleGoodMorningForUser(user);
        }
    }

    private boolean isSchedulable(Reminder reminder) {
        LocalDateTime dateTime = reminder.getDateTime();
        Boolean fired = reminder.getFired();

        return dateTime != null
                && !fired
                && dateTime.isAfter(LocalDateTime.now());
    }
}
