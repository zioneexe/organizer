package bot.tg.service;

import bot.tg.helper.TelegramHelper;
import bot.tg.model.User;
import bot.tg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BroadcastService {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    public void broadcast(String text) {
        List<User> users = this.userRepository.getAll();

        for (User user : users) {
            long userId = user.getUserId();
            TelegramHelper.sendSimpleMessage(telegramClient, userId, text);
        }
    }
}
