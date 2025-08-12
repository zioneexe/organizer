package bot.tg.handler.command.impl;

import bot.tg.handler.command.BotCommand;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TasksCommand extends BotCommand {


    private final TaskService taskService;

    @Override
    public String getCommand() {
        return "/tasks";
    }

    @Override
    public void handle(UserRequest request) {
        taskService.sendTasksFirstPage(request);
        request.getUserSession().setIdleState();
    }
}
