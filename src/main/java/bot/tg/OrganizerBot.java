package bot.tg;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.TelegramUser;
import bot.tg.handler.state.StateRecognizer;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.UserSessionService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.TelegramUserExtractor;
import bot.tg.util.sentry.TelegramUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizerBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Value("${TELEGRAM_BOT_API_KEY}")
    private String botToken;

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
                        "❓ Вибач, я не зрозумів, що ти мав на увазі.\nСпробуй /help."
                );
            }

        } finally {
            telegramUserProvider.clear();
        }
    }

    private void recognizeAndSetState(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        UserState userState = userSession.getState();
        if (userState == UserState.IDLE) {
            if (context.text != null) {
                UserState recognizedState = StateRecognizer.recognize(context.text);
                userSession.setState(recognizedState);
            }

            if (context.location != null) {
                userSession.setState(UserState.AWAITING_LOCATION);
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}
