package bot.tg.state;

import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleClientService;
import bot.tg.util.TelegramHelper;
import com.google.auth.oauth2.TokenStore;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class GoogleDisconnectHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TokenStore tokenStore;

    public GoogleDisconnectHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.tokenStore = RepositoryProvider.getTokenStore();
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();

        try {
            GoogleClientService.revokeRefreshTokenForUser(String.valueOf(userId));
            this.tokenStore.delete(String.valueOf(userId));
            userRepository.markGoogleConnected(userId, false);

            TelegramHelper.sendSimpleMessage(
                    telegramClient,
                    userId,
                    "Вітаю, ви успішно від'єднали Google Calendar!"
            );
        } catch (Exception e) {
            TelegramHelper.sendSimpleMessage(
                    telegramClient,
                    userId,
                    "Лишенько.. Сталася помилка при від'єднанні."
            );
        }

        userStateManager.setState(userId, UserState.IDLE);
    }
}
