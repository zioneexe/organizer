package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.GoogleClientService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GoogleConnectHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final GoogleClientService googleClientService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.GOOGLE_CONNECT);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        String url = googleClientService.getAuthorizationUrl(String.valueOf(context.userId));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("🔗 Авторизуватися в Google")
                                .url(url)
                                .build()
                ))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(context.userId)
                .text("Щоб підключити Google Календар, натисніть кнопку нижче:")
                .replyMarkup(keyboard)
                .build();

        TelegramHelper.safeExecute(telegramClient, message);

        userSession.setIdleState();
    }
}
