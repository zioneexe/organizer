package bot.tg.state;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

public interface StateHandler {

    Set<UserState> getSupportedStates();

    void handle(Update update);
}
