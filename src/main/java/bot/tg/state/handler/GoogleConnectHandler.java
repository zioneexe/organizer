package bot.tg.state.handler;

import bot.tg.helper.TelegramHelper;
import bot.tg.service.GoogleClientService;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GoogleConnectHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final GoogleClientService googleClientService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.GOOGLE_CONNECT);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        String url = googleClientService.getAuthorizationUrl(String.valueOf(userId));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("🔗 Авторизуватися в Google")
                                .url(url)
                                .build()
                ))
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Щоб підключити Google Календар, натисніть кнопку нижче:")
                .replyMarkup(keyboard)
                .build();

        TelegramHelper.safeExecute(telegramClient, message);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
