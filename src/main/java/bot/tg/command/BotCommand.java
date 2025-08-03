package bot.tg.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotCommand {

    String getCommand();

    void execute(Update update);
}
