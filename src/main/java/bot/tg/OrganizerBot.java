package bot.tg;

import bot.tg.dto.TelegramUser;
import bot.tg.helper.TelegramHelper;
import bot.tg.service.UserSessionService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizerBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

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

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_API_KEY");
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}
