package bot.tg.schedule;

import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import lombok.NoArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.text.MessageFormat;

@Component
@NoArgsConstructor
public class GreetingJob implements Job {

    private static final String GREETING_TEMPLATE = "🌅 Доброго ранку, {0}!\n Прокидайся і готуйся якнайкраще провести цей день :)";

    @Autowired
    private TelegramClient client;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        long userId = data.getLong("userId");
        String firstName = data.getString("firstName");

        String greetingMessage = MessageFormat.format(GREETING_TEMPLATE, firstName);
        TelegramHelper.sendSimpleMessage(client, userId, greetingMessage);

        SendMessage menuMessage = MenuHelper.formMenuMessage(userId);
        TelegramHelper.safeExecute(client, menuMessage);
    }
}