package bot.tg.state;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import bot.tg.util.PaginationHelper;
import bot.tg.util.TasksResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.ZoneId;

public class TaskSelectionHandler implements StateHandler {

    private final UserStateManager userStateManager;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TelegramClient telegramClient;

    public TaskSelectionHandler() {
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.taskRepository = RepositoryProvider.getTaskRepository();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void handle(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = PaginationHelper.formPageableForUser(Pageable.FIRST, userId, LocalDate.now(), userZoneId);
        SendMessage sendMessage = TasksResponseHelper.createTasksMessage(
                userStateManager,
                userRepository,
                taskRepository,
                pageable,
                new ChatContext(userId, chatId),
                LocalDate.now()
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
