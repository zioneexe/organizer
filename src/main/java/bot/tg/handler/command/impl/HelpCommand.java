package bot.tg.handler.command.impl;

import bot.tg.dto.TelegramContext;
import bot.tg.handler.command.BotCommand;
import bot.tg.helper.MessageHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class HelpCommand extends BotCommand {

    private final TelegramClient telegramClient;

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        String helpMessage = MessageHelper.formHelpMessage();
        TelegramHelper.sendSimpleMessage(telegramClient, context.userId, helpMessage);
    }
}
