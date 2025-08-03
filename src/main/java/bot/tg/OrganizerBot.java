package bot.tg;

import bot.tg.helper.TelegramHelper;
import bot.tg.service.UserSessionService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.util.TelegramUserExtractor;
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
    private final UserSessionService userSessionService;
    private final Dispatcher dispatcher;

    @Override
    public void consume(Update update) {
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

     /*   if (update.hasCallbackQuery()) {
            callbackDispatcher.dispatch(update);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasSticker()) {
            respondWithSticker(update);
        }

        if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasLocation())) {
            createUserIfNotExists(update);

            String text = update.getMessage().getText();
            if (text != null && text.startsWith(COMMAND_SYMBOL)) {
                handleCommand(text, update);
                return;
            }

            handleState(update);

    }

    private void respondWithSticker(Update update) {
        SendSticker sendSticker = stickerService.sendSticker(update);
        TelegramHelper.safeExecute(telegramClient, sendSticker);
    }

    private void handleCommand(String text, Update update) {
        String command = text.split(SPACE_DELIMITER)[0];
        commandRegistry.handleCommand(command, update);
    }

    private void handleState(Update update) {
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();
        Location location = update.getMessage().getLocation();

        UserState userState = userStateManager.getState(userId);
        if (userState == UserState.IDLE) {
            if (text != null) {
                UserState recognizedState = StateRecognizer.recognize(text);
                userStateManager.setState(userId, recognizedState);
            }

            if (location != null) {
                userStateManager.setState(userId, UserState.AWAITING_LOCATION);
            }

            userState = userStateManager.getState(userId);
        }

        stateDispatcher.dispatch(userState, update);
        */
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
