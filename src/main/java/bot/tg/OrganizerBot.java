package bot.tg;

import bot.tg.database.MongoConnectionManager;
import bot.tg.dto.SupportedTimeZone;
import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.state.StateRecognizer;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.StickerHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Symbol.COMMAND_SYMBOL;
import static bot.tg.constant.Symbol.SPACE_DELIMITER;

public class OrganizerBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
    public static final String DATABASE_NAME = System.getenv("DATABASE_NAME");

    private final MongoConnectionManager mongoConnectionManager;

    public OrganizerBot(String apiKey) {
        this.mongoConnectionManager = new MongoConnectionManager(CONNECTION_STRING, DATABASE_NAME);
        RepositoryProvider.init(this.mongoConnectionManager.getDatabase());
        TelegramClientProvider.init(apiKey);
        ServiceProvider.init();

        scheduleStartupJobs();
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            ServiceProvider.getCallbackDispatcher().dispatch(update);
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

    public void close() {
        mongoConnectionManager.close();
    }

    private void scheduleStartupJobs() {
        MessageService messageService = ServiceProvider.getMessageService();
        messageService.scheduleGoodMorningToAll();
        messageService.scheduleUnfiredReminders();
    }

    private void respondWithSticker(Update update) {
        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        SendSticker sendSticker = StickerHelper.sendSticker(update);
        TelegramHelper.safeExecute(telegramClient, sendSticker);
    }

    private void handleCommand(String text, Update update) {
        String command = text.split(SPACE_DELIMITER)[0];
        ServiceProvider.getCommandRegistry().handleCommand(command, update);
    }

    private void handleState(Update update) {
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();
        Location location = update.getMessage().getLocation();

        UserStateManager userStateManager = ServiceProvider.getUserStateManager();
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

        ServiceProvider.getStateDispatcher().dispatch(userState, update);
    }

    private void createUserIfNotExists(Update update) {
        String firstName = update.getMessage().getChat().getFirstName();
        String lastName = update.getMessage().getChat().getLastName();
        String username = update.getMessage().getChat().getUserName();
        long userId = update.getMessage().getFrom().getId();

        UserRepository userRepository = RepositoryProvider.getUserRepository();
        if (!userRepository.existsById(userId)) {
            userRepository.create(
                    User.builder()
                            .userId(userId)
                            .firstName(firstName)
                            .lastName(lastName)
                            .username(username)
                            .timeZone(SupportedTimeZone.getDefault().getZoneId())
                            .isGoogleConnected(false)
                            .build()
            );
        }
    }
}
