package bot.tg.callback;

import org.telegram.telegrambots.meta.api.objects.Update;

import static bot.tg.constant.Symbol.COLON_DELIMITER;
import static bot.tg.constant.Task.Callback.EDIT_TASK;

public class EditTaskHandler implements CallbackHandler {

    @Override
    public boolean supports(String data) {
        return data.startsWith(EDIT_TASK + COLON_DELIMITER);
    }

    @Override
    public void handle(Update update) {

    }
}
