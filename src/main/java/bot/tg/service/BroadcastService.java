package bot.tg.service;

import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class BroadcastService {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    public BroadcastService() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    public void broadcast(String text) {
        List<User> users = this.userRepository.getAll();

        for (User user : users) {
            long userId = user.getUserId();
            TelegramHelper.sendSimpleMessage(telegramClient, userId, text);
        }
    }
}
