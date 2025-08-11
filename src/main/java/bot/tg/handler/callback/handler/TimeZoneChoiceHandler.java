package bot.tg.handler.callback.handler;

import bot.tg.dto.SupportedTimeZone;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.callback.CallbackHandler;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.ResponseMessage.INCORRECT_REQUEST_TIMEZONE_CHANGE;
import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.TimeZone.Callback.TIMEZONE;
import static bot.tg.constant.TimeZone.Response.TIMEZONE_CHANGE;
import static bot.tg.constant.TimeZone.Response.UNSUPPORTED_TIMEZONE;

@Component
@RequiredArgsConstructor
public class TimeZoneChoiceHandler extends CallbackHandler {

    private final UserRepository userRepository;
    private final TelegramClient telegramClient;

    @Override
    public boolean supports(String data) {
        return data.startsWith(TIMEZONE + COLON_DELIMITER);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        if (context.data == null) {
            return;
        }

        String[] parts = context.data.split(COLON_DELIMITER);
        if (parts.length < 2) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, INCORRECT_REQUEST_TIMEZONE_CHANGE);
            return;
        }

        String zoneId = parts[1];

        try {
            SupportedTimeZone timeZone = SupportedTimeZone.fromZoneId(zoneId);
            userRepository.setTimeZone(context.userId, timeZone.getZoneId());

            TelegramHelper.sendSimpleMessage(
                    telegramClient,
                    context.userId,
                    TIMEZONE_CHANGE + timeZone.getDisplayName()
            );

            SendMessage menuMessage = MenuHelper.formMenuMessage(context.userId);
            TelegramHelper.safeExecute(telegramClient, menuMessage);
        } catch (IllegalArgumentException e) {
            TelegramHelper.sendSimpleMessage(telegramClient, context.userId, UNSUPPORTED_TIMEZONE);
        }

        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, context.callbackQueryId);
    }
}
