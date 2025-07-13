package bot.tg.state;

import bot.tg.model.User;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.SettingsActions.SWITCH_GOOD_MORNING_OFF;
import static bot.tg.constant.SettingsActions.SWITCH_GOOD_MORNING_ON;

public class MorningGreetingsSwitchHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public MorningGreetingsSwitchHandler() {
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
        String answer = "";
        if (text.equals(SWITCH_GOOD_MORNING_ON)) {
            this.messageService.scheduleGoodMorningForUser(user);
            answer = "Ранкові нагадування увімкнено.";
        } else if (text.equals(SWITCH_GOOD_MORNING_OFF)) {
            messageService.unscheduleGoodMorningForUser(user);
            isEnabled = false;
            answer = "Ранкові нагадування вимкнено.";
        }

        this.userRepository.markMorningGreetingsEnabled(userId, isEnabled);
        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, userId, answer);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
