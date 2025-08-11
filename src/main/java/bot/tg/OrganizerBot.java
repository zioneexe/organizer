package bot.tg;

import bot.tg.dto.TelegramUser;
import bot.tg.handler.state.StateRecognizer;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.UserSessionService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.RequestChecker;
import bot.tg.util.TelegramUserExtractor;
import bot.tg.util.sentry.TelegramUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.ResponseMessage.UNKNOWN_COMMAND;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizerBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final String apiKey;

    private final TelegramClient telegramClient;
    private final TelegramUserProvider telegramUserProvider;
    private final UserSessionService userSessionService;
    private final Dispatcher dispatcher;

    @Override
    public void consume(Update update) {
        TelegramUser telegramUser = TelegramUserExtractor.extractTelegramUser(update);
        telegramUserProvider.setCurrentTelegramUser(telegramUser);

        try {
            Long userId = TelegramUserExtractor.getUserId(update);
            UserSession userSession = userSessionService.getSession(update);
            UserRequest userRequest = UserRequest.of(update, userSession);

            recognizeAndSetState(userRequest);
            boolean dispatched = dispatcher.dispatch(userRequest);

            if (!dispatched) {
                log.warn("The request from user {} was not dispatched", userId);
                TelegramHelper.sendSimpleMessage(
                        telegramClient,
                        userId,
                        UNKNOWN_COMMAND
                );
            }

        } finally {
            telegramUserProvider.clear();
        }
    }

    private void recognizeAndSetState(UserRequest request) {
        UserSession userSession = request.getUserSession();
        UserState userState = userSession.getState();

        if (RequestChecker.isLocation(request) && userState.equals(UserState.ADJUSTING_TIMEZONE)) {
            userSession.setState(UserState.AWAITING_LOCATION);
        }

        if (!RequestChecker.isTextMessage(request) || !userState.equals(UserState.IDLE)) {
            return;
        }

        UserState recognizedState = StateRecognizer.recognize(request);
        userSession.setState(recognizedState);
    }

    @Override
    public String getBotToken() {
        return apiKey;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}
