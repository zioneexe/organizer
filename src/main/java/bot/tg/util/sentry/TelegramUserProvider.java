package bot.tg.util.sentry;

import bot.tg.dto.TelegramUser;
import io.sentry.protocol.User;
import io.sentry.spring.jakarta.SentryUserProvider;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TelegramUserProvider implements SentryUserProvider {

    private final ThreadLocal<TelegramUser> currentUser = new ThreadLocal<>();

    public void setCurrentTelegramUser(TelegramUser user) {
        currentUser.set(user);
    }

    public void clear() {
        currentUser.remove();
    }

    @Override
    public @Nullable User provideUser() {
        TelegramUser telegramUser = currentUser.get();
        if (telegramUser == null) return null;

        User sentryUser = new User();
        sentryUser.setId(String.valueOf(telegramUser.getUserId()));
        sentryUser.setUsername(telegramUser.getUsername());
        sentryUser.setUnknown(Map.of(
                "first_name", telegramUser.getFirstName(),
                "last_name", telegramUser.getLastName()
        ));

        return sentryUser;
    }

}
