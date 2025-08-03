package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.ChatContext;
import bot.tg.dto.SupportedTimeZone;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.TimeZone.Callback.TIMEZONE;

@Component
@RequiredArgsConstructor
public class TimeZoneChoiceHandler implements CallbackHandler {

    private final UserRepository userRepository;
    private final TelegramClient telegramClient;

    @Override
    public boolean supports(String data) {
        return data.startsWith(TIMEZONE + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, userId, "❌ Некоректний запит на зміну часового поясу.");
            return;
        }

        String zoneId = parts[1];

        try {
            SupportedTimeZone timeZone = SupportedTimeZone.fromZoneId(zoneId);
            userRepository.setTimeZone(userId, timeZone.getZoneId());

            TelegramHelper.sendSimpleMessage(
                    telegramClient,
                    userId,
                    "✅ Твій часовий пояс змінено на:\n" + timeZone.getDisplayName()
            );

            SendMessage menuMessage = MenuHelper.formMenuMessage(new ChatContext(userId, userId));
            TelegramHelper.safeExecute(telegramClient, menuMessage);
        } catch (IllegalArgumentException e) {
            TelegramHelper.sendSimpleMessage(telegramClient, userId, "⛔ Непідтримуваний часовий пояс");
        }

        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
