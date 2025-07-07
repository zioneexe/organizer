package bot.tg.state;

import bot.tg.provider.TelegramClientProvider;
import bot.tg.server.GoogleOAuthService;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class GoogleConnectHandler implements StateHandler {

    private final TelegramClient telegramClient;

    public GoogleConnectHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        String url = GoogleOAuthService.getAuthorizationUrl(String.valueOf(userId));

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
    }
}
