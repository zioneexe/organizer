package bot.tg.state;

import bot.tg.dto.create.TaskCreateDto;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.util.Constants.TASK_DESCRIPTION;

public class TaskTitleHandler implements StateHandler {

    private final UserStateManager userStateManager;
    private final TelegramClient telegramClient;

    public TaskTitleHandler() {
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.telegramClient = TelegramClientProvider.getInstance();
    }

    @Override
    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String text = update.getMessage().getText();

            userStateManager.setState(userId, UserState.AWAITING_TASK_DESCRIPTION);

            TaskCreateDto dto = userStateManager.getTaskDraft(userId);
            dto.setTitle(text);

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(TASK_DESCRIPTION)
                    .replyMarkup(ForceReplyKeyboard.builder().forceReply(true).build())
                    .build();

            TelegramHelper.safeExecute(telegramClient, message);
        }
    }
}
