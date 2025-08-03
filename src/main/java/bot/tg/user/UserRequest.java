package bot.tg.user;

import bot.tg.dto.TelegramContext;
import lombok.Builder;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

@Builder
@Getter
public class UserRequest {

    private final UserSession userSession;
    private final Update update;
    private final TelegramContext context;

    public static UserRequest of(Update update, UserSession userSession) {
        return UserRequest.builder()
                .update(update)
                .context(new TelegramContext(update))
                .userSession(userSession)
                .build();
    }
}
