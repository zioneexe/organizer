package bot.tg.callback.handler;

import bot.tg.callback.CallbackHandler;
import bot.tg.dto.ChatContext;
import bot.tg.dto.create.TaskCreateDto;
import bot.tg.mapper.TaskMapper;
import bot.tg.model.TodoTask;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.service.TaskService;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Task.Callback.SKIP_DESCRIPTION_TASK;

public class SkipDescriptionHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final TaskService taskService;

    public SkipDescriptionHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.taskService = new TaskService();
    }

    @Override
    public boolean supports(String data) {
        return data.equals(SKIP_DESCRIPTION_TASK);
    }

    @Override
    public void handle(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long userId = update.getCallbackQuery().getFrom().getId();
        String callbackQueryId = update.getCallbackQuery().getId();

        TelegramHelper.sendSimpleMessage(telegramClient, userId, "Окей, без опису 👍");

        TaskCreateDto dto = userStateManager.getTaskDraft(userId);
        dto.setDescription(null);
        TodoTask task = TaskMapper.fromDto(dto);
        taskService.endTaskCreation(task, new ChatContext(userId, chatId));

        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
