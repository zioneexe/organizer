package bot.tg.schedule;

import bot.tg.provider.TelegramClientProvider;
import bot.tg.util.TelegramHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class GoodMorningJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        long userId = data.getLong("userId");
        String firstName = data.getString("firstName");

        String msg = "🌅 Доброго ранку, " + firstName +
                "! \n Прокидайся і готуйся якнайкраще провести цей день :)";

        TelegramClient client = TelegramClientProvider.getInstance();
        TelegramHelper.sendSimpleMessage(client, userId, msg);
    }
}

