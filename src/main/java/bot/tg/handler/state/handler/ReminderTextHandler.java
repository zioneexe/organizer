package bot.tg.handler.state.handler;

import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.TelegramHelper;
import bot.tg.mapper.ReminderMapper;
import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleCalendarService;
import bot.tg.service.MessageService;
import bot.tg.service.ReminderService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import bot.tg.util.validation.Violation;
import bot.tg.util.validation.impl.TaskAndReminderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Set;

import static bot.tg.constant.Google.GOOGLE_EVENT_ADDED;
import static bot.tg.constant.Reminder.Response.REMINDER_CREATED;

@Component
@RequiredArgsConstructor
public class ReminderTextHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final MessageService messageService;
    private final GoogleCalendarService googleCalendarService;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final TaskAndReminderValidator validator;
    private final ReminderService reminderService;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_REMINDER_TEXT);
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

        userSession.setIdleState();

        ReminderCreateDto dto = userSession.getReminderDraft();
        dto.setText(context.text);

        Reminder reminder = ReminderMapper.fromDto(dto);
        String reminderId = reminderRepository.create(reminder);
        messageService.scheduleReminder(reminder);

        StringBuilder replyTextBuilder = new StringBuilder(REMINDER_CREATED);
        boolean isConnected = userRepository.isGoogleConnected(context.userId);
        if (isConnected) {
            this.googleCalendarService.createCalendarEventAndReturnLink(context.userId, reminderId, dto)
                    .ifPresent(calendarLink -> replyTextBuilder
                            .append("\n\n")
                            .append(GOOGLE_EVENT_ADDED)
                            .append(calendarLink));
        }

        TelegramHelper.sendSimpleMessage(telegramClient, context.userId, replyTextBuilder.toString());

        reminderService.sendRemindersFirstPage(request);
    }

}
