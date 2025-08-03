package bot.tg.handler.command.impl;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.command.BotCommand;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class StartCommand extends BotCommand {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        SendMessage message = MenuHelper.formMenuMessage(context.userId);
        TelegramHelper.safeExecute(telegramClient, message);

        userStateManager.setState(context.userId, UserState.IDLE);
    }
}
