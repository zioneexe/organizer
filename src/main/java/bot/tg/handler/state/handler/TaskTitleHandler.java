package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.TaskCreateDto;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.validation.Violation;
import bot.tg.util.validation.impl.TaskAndReminderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Set;

import static bot.tg.constant.Task.Callback.SKIP_DESCRIPTION_TASK;
import static bot.tg.constant.Task.Response.TASK_DESCRIPTION;
import static bot.tg.constant.Task.Response.TASK_NO_DESCRIPTION;

@Component
@RequiredArgsConstructor
public class TaskTitleHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final TaskAndReminderValidator validator;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_TASK_TITLE);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        List<Violation> violations = validator.validateTitle(context.text);
        if (!violations.isEmpty()) {
            TelegramHelper.sendMessageWithForceReply(
                    telegramClient,
                    context.userId,
                    violations.getFirst().getMessage()
            );
            return;
        }

        userSession.setState(UserState.AWAITING_TASK_DESCRIPTION);

        TaskCreateDto dto = userSession.getTaskDraft();
        dto.setTitle(context.text);

        List<InlineKeyboardRow> keyboard = List.of(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder()
                        .text(TASK_NO_DESCRIPTION)
                        .callbackData(SKIP_DESCRIPTION_TASK)
                        .build()
                )
        ));

        TelegramHelper.sendMessageWithMarkup(telegramClient, context.userId,
                TASK_DESCRIPTION,
                InlineKeyboardMarkup.builder()
                        .keyboard(keyboard)
                        .build()
        );
    }
}
