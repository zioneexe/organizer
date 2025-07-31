package bot.tg.command;

import bot.tg.command.impl.*;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Map;

import static bot.tg.constant.ResponseMessage.UNKNOWN_COMMAND;

public class CommandRegistry {

    private final Map<String, BotCommand> commands = new HashMap<>();

    private final TelegramClient telegramClient;

    public CommandRegistry() {
        this.telegramClient = TelegramClientProvider.getInstance();

        register("/start", new StartCommand());
        register("/tasks", new TasksCommand());
        register("/reminders", new RemindersCommand());
        register("/newtask", new NewTaskCommand());
        register("/newreminder", new NewReminderCommand());
    }

    public void register(String commandName, BotCommand command) {
        commands.put(commandName, command);
    }

    public void handleCommand(String commandName, Update update) {
        BotCommand command = commands.get(commandName);

        if (command != null) {
            command.execute(update);
        } else {
            long chatId = update.getMessage().getChatId();
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, UNKNOWN_COMMAND);
        }
    }


}
