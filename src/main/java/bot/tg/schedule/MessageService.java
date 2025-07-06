package bot.tg.schedule;

import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class MessageService {

    private final TelegramClient client;

    private final UserRepository repository;

    public MessageService() {
        this.client = TelegramClientProvider.getInstance();
        this.repository = RepositoryProvider.getInstance().getUserRepository();
    }

    public void sendGoodMorningToAll() {
        List<User> users = repository.getAll();

        for (User user : users) {
            long userId = user.getUserId();

            SendMessage message = SendMessage.builder()
                    .chatId(userId)
                    .text("🌅 Доброго ранку, " + user.getFirstName() + "! \n Прокидайся і готуйся якнайкраще провести цей день :)")
                    .build();
            TelegramHelper.safeExecute(client, message);
        }
    }
}
