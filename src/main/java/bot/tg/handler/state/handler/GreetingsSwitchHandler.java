package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.model.User;
import bot.tg.repository.UserRepository;
import bot.tg.service.MessageService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Set;

import static bot.tg.constant.Greetings.Button.SWITCH_GREETING_OFF;
import static bot.tg.constant.Greetings.Button.SWITCH_GREETING_ON;
import static bot.tg.constant.Greetings.Response.SWITCHED_GREETING_OFF;
import static bot.tg.constant.Greetings.Response.SWITCHED_GREETING_ON;

@Component
@RequiredArgsConstructor
public class GreetingsSwitchHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.SWITCH_GREETING);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        if (context.text == null) {
            return;
        }

        boolean isEnabled = true;

        String answer = "";
        User user = userRepository.getById(context.userId);
        if (context.text.equals(SWITCH_GREETING_ON)) {
            messageService.scheduleGreetingForUser(user);
            answer = SWITCHED_GREETING_ON;
        } else if (context.text.equals(SWITCH_GREETING_OFF)) {
            messageService.cancelGreetingForUser(user);
            isEnabled = false;
            answer = SWITCHED_GREETING_OFF;
        }

        userRepository.setGreetingsEnabled(context.userId, isEnabled);
        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, answer);

        userSession.setIdleState();
    }
}
