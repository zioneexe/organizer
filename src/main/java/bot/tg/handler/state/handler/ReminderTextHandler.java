package bot.tg.handler.state.handler;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.handler.state.StateHandler;
import bot.tg.helper.ReminderResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.mapper.ReminderMapper;
import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleCalendarService;
import bot.tg.service.MessageService;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import bot.tg.validation.TaskAndReminderValidator;
import bot.tg.validation.Violation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static bot.tg.constant.Reminder.Response.REMINDER_CREATED;

@Component
@RequiredArgsConstructor
public class ReminderTextHandler extends StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final GoogleCalendarService googleCalendarService;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final PaginationService paginationService;
    private final TaskAndReminderValidator validator;

    @Override
    public Set<UserState> getSupportedStates() {
        return Set.of(UserState.AWAITING_REMINDER_TEXT);
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        List<Violation> violations = validator.validateTitle(context.text);
        if (!violations.isEmpty()) {
            TelegramHelper.sendMessageWithForceReply(
                    telegramClient,
                    context.userId,
                    violations.getFirst().getMessage()
            );
            return;
        }

        userStateManager.setState(context.userId, UserState.IDLE);

        ReminderCreateDto dto = userStateManager.getReminderDraft(context.userId);
        dto.setText(context.text);

        Reminder reminder = ReminderMapper.fromDto(dto);
        String reminderId = reminderRepository.create(reminder);
        messageService.scheduleReminder(reminder);

        StringBuilder replyTextBuilder = new StringBuilder(REMINDER_CREATED);
        boolean isConnected = userRepository.isGoogleConnected(context.userId);
        if (isConnected) {
            this.googleCalendarService.createCalendarEventAndReturnLink(context.userId, reminderId, dto)
                    .ifPresent(calendarLink -> replyTextBuilder
                            .append("\n\nПодія додана в Google Календар: ")
                            .append(calendarLink));
        }

        TelegramHelper.sendSimpleMessage(telegramClient, context.userId, replyTextBuilder.toString());

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = paginationService.formReminderPageableForUser(Pageable.FIRST, context.userId, userZoneId);
        SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(
                userStateManager,
                userRepository,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, remindersMessage);
    }

}
