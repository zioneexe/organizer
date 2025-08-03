package bot.tg.handler.command.impl;

import bot.tg.handler.command.BotCommand;
import bot.tg.service.TaskService;
import bot.tg.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewTaskCommand extends BotCommand {

    private final TaskService taskService;

    @Override
    public String getCommand() {
        return "/newtask";
    }

    @Override
    public void handle(UserRequest request) {
        this.taskService.startTaskCreation(request.getContext());
    }
}
