package bot.tg.schedule;

import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.util.TelegramHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ReminderJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext)  {
        JobDataMap data = jobExecutionContext.getMergedJobDataMap();

        String reminderId = data.getString("reminderId");
        long userId = data.getLong("userId");
        String text = data.getString("text");

        UserRepository userRepository = RepositoryProvider.getUserRepository();
        User user = userRepository.getById(userId);
        ZoneId userTimeZone = user != null && user.getTimeZone() != null ? ZoneId.of(user.getTimeZone()) : ZoneId.systemDefault();

        ReminderRepository reminderRepository = RepositoryProvider.getReminderRepository();
        reminderRepository.markAsFired(reminderId);

        LocalDateTime reminderTime = reminderRepository.getById(reminderId).getDateTime();
        ZonedDateTime userZonedDateTime = reminderTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(userTimeZone);

        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        String message = "🔔 Нагадування на " + userZonedDateTime.toLocalTime() + ": " + text;
        TelegramHelper.sendSimpleMessage(telegramClient, userId, message);
    }
}
