package bot.tg.command.impl;

import bot.tg.command.BotCommand;
import bot.tg.dto.ChatContext;
import bot.tg.helper.MenuHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class StartCommand implements BotCommand {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void execute(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        SendMessage message = MenuHelper.formMenuMessage(new ChatContext(userId, chatId));
        TelegramHelper.safeExecute(telegramClient, message);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
