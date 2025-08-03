package bot.tg.schedule;

import bot.tg.dto.ChatContext;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class GreetingJob implements Job {

    private final TelegramClient client;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        long userId = data.getLong("userId");
        String firstName = data.getString("firstName");

        String greetingMessage = "🌅 Доброго ранку, " + firstName +
                "! \n Прокидайся і готуйся якнайкраще провести цей день :)";

        TelegramHelper.sendSimpleMessage(client, userId, greetingMessage);

        SendMessage menuMessage = MenuHelper.formMenuMessage(new ChatContext(userId, userId));
        TelegramHelper.safeExecute(client, menuMessage);
    }
}