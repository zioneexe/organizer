package bot.tg.callback;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class CallbackDispatcher {

    private final List<CallbackHandler> handlers = new ArrayList<>();

    public CallbackDispatcher() {
       register(new TaskStatusHandler());
       register(new TaskDetailsHandler());
       register(new ReminderDateHandler());
       register(new ReminderTimePickerHandler());
       register(new BackToTasksHandler());
    }

    public void register(CallbackHandler handler) {
        handlers.add(handler);
    }

    public void dispatch(Update update) {
        String data = update.getCallbackQuery().getData();
        handlers.stream()
                .filter(h -> h.supports(data))
                .findFirst()
                .ifPresent(h -> h.handle(update));
    }
}
