package bot.tg.state.handler;

import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Greetings.Button.SWITCH_GREETING_OFF;
import static bot.tg.constant.Greetings.Button.SWITCH_GREETING_ON;

public class GreetingsSwitchHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public GreetingsSwitchHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.messageService = ServiceProvider.getMessageService();
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        User user = this.userRepository.getById(userId);

        boolean isEnabled = true;
        String answer = "Ранкові привітання ";
        if (text.equals(SWITCH_GREETING_ON)) {
            this.messageService.scheduleGreetingForUser(user);
            answer += "увімкнено.";
        } else if (text.equals(SWITCH_GREETING_OFF)) {
            messageService.cancelGreetingForUser(user);
            isEnabled = false;
            answer += "вимкнено.";
        }

        this.userRepository.setGreetingsEnabled(userId, isEnabled);
        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, userId, answer);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
