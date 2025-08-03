package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.Time;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimePickerResponseHelper;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GreetingsTimeChoiceHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.CHOOSING_GREETINGS_TIME);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        Time currentPreferredTime = userRepository.getPreferredGreetingTime(context.userId);
        this.userStateManager.setMorningGreetingTimeDraft(context.userId, currentPreferredTime);
        SendMessage greetingTimePickerMessage = TimePickerResponseHelper.createGreetingTimePickerMessage(context, userRepository);
        TelegramHelper.safeExecute(telegramClient, greetingTimePickerMessage);
    }

}
