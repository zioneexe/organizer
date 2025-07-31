package bot.tg.state.handler;

import bot.tg.dto.Time;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.state.StateHandler;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimePickerResponseHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class GreetingsTimeChoiceHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;

    public GreetingsTimeChoiceHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();

        Time currentPreferredTime = userRepository.getPreferredGreetingTime(userId);
        this.userStateManager.setMorningGreetingTimeDraft(userId, currentPreferredTime);
        SendMessage greetingTimePickerMessage = TimePickerResponseHelper.createGreetingTimePickerMessage(update);
        TelegramHelper.safeExecute(telegramClient, greetingTimePickerMessage);
    }
}
