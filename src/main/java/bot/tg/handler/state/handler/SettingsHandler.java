package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
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
public class SettingsHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.SETTINGS);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        SendMessage settingsMenu = MenuHelper.formSettingsMenu(userRepository, context.userId);
        TelegramHelper.safeExecute(telegramClient, settingsMenu);

        userStateManager.setState(context.userId, UserState.IDLE);
    }
}
