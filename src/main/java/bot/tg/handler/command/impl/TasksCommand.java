package bot.tg.handler.command.impl;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.command.BotCommand;
import bot.tg.helper.TasksResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class TasksCommand extends BotCommand {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PaginationService paginationService;

    @Override
    public String getCommand() {
        return "/tasks";
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = paginationService.formTaskPageableForUser(Pageable.FIRST, context.userId, LocalDate.now(), userZoneId);
        SendMessage sendMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                context.userId,
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        userStateManager.setState(context.userId, UserState.IDLE);
    }
}
