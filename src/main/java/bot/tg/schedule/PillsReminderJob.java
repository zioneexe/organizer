package bot.tg.schedule;

import bot.tg.helper.TelegramHelper;
import lombok.NoArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Response.PILLS_REMINDER_MESSAGE;

@Component
@NoArgsConstructor
public class PillsReminderJob implements Job {

    @Autowired
    private TelegramClient client;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        long userId = data.getLong("userId");

        TelegramHelper.sendSimpleMessage(client, userId, PILLS_REMINDER_MESSAGE);
    }
}
