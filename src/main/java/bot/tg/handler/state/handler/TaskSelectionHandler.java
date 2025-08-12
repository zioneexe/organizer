package bot.tg.handler.state.handler;

import bot.tg.handler.state.StateHandler;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskSelectionHandler extends StateHandler {

    private final TaskService taskService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_TASK_SELECTION);
    }

    @Override
    public void handle(UserRequest request) {
        taskService.sendTasksFirstPage(request);
        request.getUserSession().setIdleState();
    }
}
