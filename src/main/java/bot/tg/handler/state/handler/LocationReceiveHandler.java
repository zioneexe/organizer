package bot.tg.handler.state.handler;

import bot.tg.dto.SupportedTimeZone;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.UserRepository;
import bot.tg.service.TimeZoneService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

import static bot.tg.constant.TimeZone.Response.TIMEZONE_DETECTED_AUTOMATICALLY;

@Component
@RequiredArgsConstructor
public class LocationReceiveHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final TimeZoneService timeZoneService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_LOCATION);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.location == null) {
            return;
        }

        double latitude = context.location.getLatitude();
        double longitude = context.location.getLongitude();

        String zoneId = timeZoneService.resolveZoneId(latitude, longitude);
        SupportedTimeZone timeZone = SupportedTimeZone.fromZoneId(zoneId);

        userRepository.setTimeZone(context.userId, zoneId);
        userRepository.saveUserLocation(context.userId, latitude, longitude);

        TelegramHelper.sendSimpleMessage(
                telegramClient,
                context.userId,
                TIMEZONE_DETECTED_AUTOMATICALLY + timeZone.getDisplayName()
        );

        SendMessage menuMessage = MenuHelper.formMenuMessage(context.userId);
        TelegramHelper.safeExecute(telegramClient, menuMessage);

        userSession.setIdleState();
    }
}
