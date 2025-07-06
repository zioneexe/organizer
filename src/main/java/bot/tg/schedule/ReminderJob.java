package bot.tg.schedule;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.util.TelegramHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class ReminderJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext)  {
        JobDataMap data = jobExecutionContext.getMergedJobDataMap();

        String reminderId = data.getString("reminderId");
        long userId = data.getLong("userId");
        String text = data.getString("text");

        ReminderRepository reminderRepository = RepositoryProvider.getReminderRepository();
        reminderRepository.markAsFired(reminderId);

        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        TelegramHelper.sendSimpleMessage(telegramClient, userId, "🔔 Нагадування: " + text);
    }
}
