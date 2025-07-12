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
        register(new EditTaskHandler());
        register(new DeleteTaskHandler());
        register(new DeleteReminderHandler());
        register(new ReminderTimePickerHandler());
        register(new BackToTasksHandler());
        register(new IgnoreHandler());
        register(new NewReminderHandler());
        register(new NewTaskHandler());
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
