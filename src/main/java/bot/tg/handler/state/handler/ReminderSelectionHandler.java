package bot.tg.handler.state.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.ReminderResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReminderSelectionHandler extends StateHandler {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final TelegramClient telegramClient;
    private final PaginationService paginationService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_REMINDER_SELECTION);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = paginationService.formReminderPageableForUser(Pageable.FIRST, context.userId, userZoneId);
        SendMessage sendMessage = ReminderResponseHelper.createRemindersMessage(
                userSession,
                userRepository,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        userSession.setIdleState();
    }
}
