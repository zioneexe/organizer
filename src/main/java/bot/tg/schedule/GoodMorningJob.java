package bot.tg.schedule;

import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.util.TelegramHelper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class GoodMorningJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        UserRepository userRepository = RepositoryProvider.getUserRepository();
        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        List<User> users = userRepository.getAll();

        for (User user : users) {
            long userId = user.getUserId();
            String goodMorning = "🌅 Доброго ранку, " +
                    user.getFirstName() +
                    "! \n Прокидайся і готуйся якнайкраще провести цей день :)";
            TelegramHelper.sendSimpleMessage(telegramClient, userId, goodMorning);
        }
    }
}
