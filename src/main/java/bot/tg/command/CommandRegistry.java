package bot.tg.command;

import bot.tg.provider.TelegramClientProvider;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, BotCommand> commands = new HashMap<>();

    private final TelegramClient telegramClient;

    public CommandRegistry() {
        this.telegramClient = TelegramClientProvider.getInstance();

        register("/tasks", new TasksCommand());
        register("/start", new StartCommand());
        register("/newtask", new NewTaskCommand());
    }

    public void register(String commandName, BotCommand command) {
        commands.put(commandName, command);
    }

    public void handleCommand(String commandName, Update update) {
        BotCommand command = commands.get(commandName);

        if (command != null) {
            command.execute(update);
        } else {
            SendMessage message = SendMessage.builder().
                    chatId(update.getMessage().getChatId().toString())
                    .text("Невідома команда.")
                    .build();
            TelegramHelper.safeExecute(telegramClient, message);
        }
    }


}
