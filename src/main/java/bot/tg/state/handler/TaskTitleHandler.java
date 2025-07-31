package bot.tg.state.handler;

import bot.tg.dto.create.TaskCreateDto;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static bot.tg.constant.Task.Callback.SKIP_DESCRIPTION_TASK;
import static bot.tg.constant.Task.Response.TASK_DESCRIPTION;

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

            if (text.length() > 40) {
                TelegramHelper.sendMessageWithForceReply(
                        telegramClient,
                        chatId,
                        "Назва занадто довга. 🙈 Скороти до 40 символів."
                );
                return;
            }

            userStateManager.setState(userId, UserState.AWAITING_TASK_DESCRIPTION);

            TaskCreateDto dto = userStateManager.getTaskDraft(userId);
            dto.setTitle(text);

            List<InlineKeyboardRow> keyboard = List.of(new InlineKeyboardRow(
                    List.of(InlineKeyboardButton.builder()
                            .text("Без опису")
                            .callbackData(SKIP_DESCRIPTION_TASK)
                            .build()
                    )
            ));

            TelegramHelper.sendMessageWithMarkup(telegramClient, chatId,
                    TASK_DESCRIPTION,
                    InlineKeyboardMarkup.builder()
                            .keyboard(keyboard)
                            .build()
            );
        }
    }
}
