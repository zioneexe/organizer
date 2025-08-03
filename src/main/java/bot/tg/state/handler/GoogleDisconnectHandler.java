package bot.tg.state.handler;

import bot.tg.helper.TelegramHelper;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleClientService;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import com.google.auth.oauth2.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GoogleDisconnectHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TokenStore tokenStore;
    private final GoogleClientService googleClientService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.GOOGLE_DISCONNECT);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();

        try {
            googleClientService.revokeRefreshTokenForUser(String.valueOf(userId));
            this.tokenStore.delete(String.valueOf(userId));
            userRepository.setGoogleConnected(userId, false);

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
