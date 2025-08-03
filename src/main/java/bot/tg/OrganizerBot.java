package bot.tg;

import bot.tg.callback.CallbackDispatcher;
import bot.tg.command.CommandRegistry;
import bot.tg.dto.SupportedTimeZone;
import bot.tg.dto.Time;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.User;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.service.StickerService;
import bot.tg.state.StateDispatcher;
import bot.tg.state.StateRecognizer;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Symbol.COMMAND_SYMBOL;
import static bot.tg.constant.Symbol.SPACE_DELIMITER;

@Component
@RequiredArgsConstructor
public class OrganizerBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final CallbackDispatcher callbackDispatcher;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final CommandRegistry commandRegistry;
    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final StateDispatcher stateDispatcher;
    private final StickerService stickerService;

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
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
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_API_KEY");
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    private void scheduleStartupJobs() {
        messageService.scheduleGreetingsToAll();
        messageService.scheduleUnfiredReminders();
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
    }

    private void createUserIfNotExists(Update update) {
        String firstName = update.getMessage().getChat().getFirstName();
        String lastName = update.getMessage().getChat().getLastName();
        String username = update.getMessage().getChat().getUserName();
        long userId = update.getMessage().getFrom().getId();

        if (!userRepository.existsById(userId)) {
            userRepository.create(
                    User.builder()
                            .userId(userId)
                            .firstName(firstName)
                            .lastName(lastName)
                            .username(username)
                            .timeZone(SupportedTimeZone.getDefault().getZoneId())
                            .isGoogleConnected(false)
                            .greetingsEnabled(true)
                            .preferredGreetingTime(Time.DEFAULT_REMINDER_TIME)
                            .build()
            );
        }
    }
}
