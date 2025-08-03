package bot.tg;

import bot.tg.handler.RequestHandler;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Dispatcher {

    private final List<RequestHandler> handlers;

    public boolean dispatch(UserRequest request) {
        return handlers.stream()
                .filter(handler -> handler.isApplicable(request))
                .findFirst()
                .map(handler -> {
                    handler.handle(request);
                    return true;
                })
                .orElse(false);
    }
}
