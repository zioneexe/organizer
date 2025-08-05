package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.Time;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.helper.TimePickerResponseHelper;
import bot.tg.repository.UserRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GreetingsTimeChoiceHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.CHOOSING_GREETINGS_TIME);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        Time currentPreferredTime = userRepository.getPreferredGreetingTime(context.userId);
        userSession.setMorningGreetingTimeDraft(currentPreferredTime);
        SendMessage greetingTimePickerMessage = TimePickerResponseHelper.createGreetingTimePickerMessage(context, userRepository);
        TelegramHelper.safeExecute(telegramClient, greetingTimePickerMessage);
    }

}
