package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleClientService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import com.google.auth.oauth2.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GoogleDisconnectHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final TokenStore tokenStore;
    private final GoogleClientService googleClientService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.GOOGLE_DISCONNECT);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        try {
            googleClientService.revokeRefreshTokenForUser(String.valueOf(context.userId));
            tokenStore.delete(String.valueOf(context.userId));
            userRepository.setGoogleConnected(context.userId, false);

            TelegramHelper.sendSimpleMessage(
                    telegramClient,
                    context.userId,
                    "Вітаю, ви успішно від'єднали Google Calendar!"
            );
        } catch (Exception e) {
            TelegramHelper.sendSimpleMessage(
                    telegramClient,
                    context.userId,
                    "Лишенько.. Сталася помилка при від'єднанні."
            );
        }

        userSession.setIdleState();
    }
}
