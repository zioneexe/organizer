package bot.tg.handler.callback.handler;

import bot.tg.handler.callback.CallbackHandler;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static bot.tg.constant.Task.Callback.BACK_TO_TASKS;

@Component
@RequiredArgsConstructor
public class BackToTasksHandler extends CallbackHandler {

    private final TaskService taskService;

    @Override
    public boolean supports(String data) {
        return data.equals(BACK_TO_TASKS);
    }

    @Override
    public void handle(UserRequest request) {
        taskService.sendTasksPageEdit(request);
        request.getUserSession().setIdleState();
    }
}
