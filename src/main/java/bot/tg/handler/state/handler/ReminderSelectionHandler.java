package bot.tg.handler.state.handler;

import bot.tg.handler.state.StateHandler;
import bot.tg.service.ReminderService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReminderSelectionHandler extends StateHandler {

    private final ReminderService reminderService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_REMINDER_SELECTION);
    }

    @Override
    public void handle(UserRequest request) {
        reminderService.sendRemindersFirstPage(request);
        request.getUserSession().setIdleState();
    }
}
