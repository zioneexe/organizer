package bot.tg.state;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface StateHandler {

    void handle(Update update);
}
