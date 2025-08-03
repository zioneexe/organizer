package bot.tg.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CallbackDispatcher {

    private final List<CallbackHandler> handlers;

    public void dispatch(Update update) {
        String data = update.getCallbackQuery().getData();
        handlers.stream()
                .filter(handler -> handler.supports(data))
                .findFirst()
                .ifPresent(handler -> handler.handle(update));
    }
}
