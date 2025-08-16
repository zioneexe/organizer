package bot.tg.service;

import bot.tg.dto.SupportedTimeZone;
import bot.tg.dto.Time;
import bot.tg.model.User;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.TelegramUserExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

import static bot.tg.constant.Core.DEFAULT_CALENDAR_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserRepository userRepository;

    private final Map<Long, UserSession> userSessionMap = new HashMap<>();

    public UserSession getSession(Update update) {
        Long userId = TelegramUserExtractor.getUserId(update);
        log.debug("Getting session for userId={}", userId);

        createUserIfNotExists(update);
        return userSessionMap.computeIfAbsent(userId, id -> {
            log.debug("Creating new session for userId={}", id);
            return UserSession.builder()
                    .state(UserState.IDLE)
                    .userId(id)
                    .build();
        });
    }

    private void createUserIfNotExists(Update update) {
        String firstName = TelegramUserExtractor.getFirstName(update);
        String lastName = TelegramUserExtractor.getLastName(update);
        String username = TelegramUserExtractor.getUsername(update);
        Long userId = TelegramUserExtractor.getUserId(update);

        if (!userRepository.existsById(userId)) {
            log.info("Creating new user in DB: userId={}, username={}", userId, username);
            userRepository.create(
                    User.builder()
                            .userId(userId)
                            .firstName(firstName)
                            .lastName(lastName)
                            .username(username)
                            .timeZone(SupportedTimeZone.getDefault().getZoneId())
                            .isGoogleConnected(false)
                            .calendarId(DEFAULT_CALENDAR_ID)
                            .greetingsEnabled(true)
                            .preferredGreetingTime(Time.DEFAULT_REMINDER_TIME)
                            .build()
            );
        } else {
            log.debug("User already exists in DB: userId={}", userId);
        }
    }
}
