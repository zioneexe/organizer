package bot.tg;

import bot.tg.callback.CallbackDispatcher;
import bot.tg.command.CommandRegistry;
import bot.tg.database.MongoConnectionManager;
import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.state.StateDispatcher;
import bot.tg.state.StateRecognizer;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.StickerHelper;
import bot.tg.util.TelegramHelper;
import com.mongodb.client.MongoDatabase;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Core.DATABASE_NAME;
import static bot.tg.constant.Symbol.COMMAND_SYMBOL;
import static bot.tg.constant.Symbol.SPACE_DELIMITER;
import static bot.tg.schedule.MessageScheduler.DEFAULT_TIMEZONE;

public class OrganizerBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String CONNECTION_STRING = System.getenv("CONNECTION_STRING");

    private final CommandRegistry commandRegistry;
    private final MongoConnectionManager mongoConnectionManager;

    private final UserRepository userRepository;

    private final UserStateManager userStateManager;
    private final StateDispatcher stateDispatcher;
    private final CallbackDispatcher callbackDispatcher;

    public OrganizerBot(String apiKey) {
        this.mongoConnectionManager = new MongoConnectionManager(CONNECTION_STRING, DATABASE_NAME);
        MongoDatabase database = mongoConnectionManager.getDatabase();
        RepositoryProvider.init(database);
        TelegramClientProvider.init(apiKey);
        ServiceProvider.init();

        this.commandRegistry = ServiceProvider.getCommandRegistry();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.stateDispatcher = ServiceProvider.getStateDispatcher();
        this.callbackDispatcher = ServiceProvider.getCallbackDispatcher();

        this.userRepository = RepositoryProvider.getUserRepository();

        MessageService messageService = ServiceProvider.getMessageService();
        messageService.scheduleGoodMorningToAll();
        messageService.scheduleUnfiredReminders();
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            callbackDispatcher.dispatch(update);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasSticker()) {
            respondWithSticker(update);
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            createUserIfNotExists(update);

            String text = update.getMessage().getText();
            if (text.startsWith(COMMAND_SYMBOL)) {
                handleCommand(text, update);
                return;
            }

            handleState(update);
        }
    }

    public void close() {
        mongoConnectionManager.close();
    }

    private void respondWithSticker(Update update) {
        TelegramClient telegramClient = TelegramClientProvider.getInstance();
        SendSticker sendSticker = StickerHelper.sendSticker(update);
        TelegramHelper.safeExecute(telegramClient, sendSticker);
    }

    private void handleCommand(String text, Update update) {
        String command = text.split(SPACE_DELIMITER)[0];
        commandRegistry.handleCommand(command, update);
    }

    private void handleState(Update update) {
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();

        UserState userState = userStateManager.getState(userId);
        if (userState == UserState.IDLE) {
            UserState recognizedState = StateRecognizer.recognize(text);
            userStateManager.setState(userId, recognizedState);
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
                            .timeZone(DEFAULT_TIMEZONE)
                            .isGoogleConnected(false)
                            .build()
            );
        }
    }
}
