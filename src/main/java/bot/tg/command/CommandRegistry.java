package bot.tg.command;

import bot.tg.helper.TelegramHelper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bot.tg.constant.ResponseMessage.UNKNOWN_COMMAND;

@Component
public class CommandRegistry {

    private final Map<String, BotCommand> commands = new HashMap<>();

    private final TelegramClient telegramClient;

    public CommandRegistry(TelegramClient telegramClient, List<BotCommand> commands) {
        this.telegramClient = telegramClient;

        for (BotCommand command : commands) {
            this.commands.put(command.getCommand(), command);
        }
    }

    public void handleCommand(String commandName, Update update) {

        BotCommand command = commands.get(commandName);

        if (command == null) {
            long chatId = update.getMessage().getChatId();
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, UNKNOWN_COMMAND);
            return;
        }

        command.execute(update);
    }


}
