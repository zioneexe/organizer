package bot.tg.schedule;

import bot.tg.model.Reminder;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.ReminderRepository;

import java.time.LocalDateTime;

public class MessageService {

    private final MessageScheduler messageScheduler;
    private final ReminderRepository reminderRepository;

    public MessageService() {
        this.reminderRepository = RepositoryProvider.getReminderRepository();
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
        messageScheduler.scheduleGoodMorningToAll();
    }

    private boolean isSchedulable(Reminder reminder) {
        LocalDateTime dateTime = reminder.getDateTime();
        Boolean fired = reminder.getFired();

        return dateTime != null
                && !fired
                && dateTime.isAfter(LocalDateTime.now());
    }
}
