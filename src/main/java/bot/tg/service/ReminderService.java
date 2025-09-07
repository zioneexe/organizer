package bot.tg.service;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.helper.ReminderHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.ReminderRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Core.Response.ALRIGHT;
import static bot.tg.constant.Reminder.Response.REMINDER_DATE;
import static bot.tg.helper.ReminderHelper.formDateChoiceKeyboard;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final TelegramClient telegramClient;
    private final TimeZoneService timeZoneService;
    private final ReminderRepository reminderRepository;
    private final PaginationService paginationService;

    public void sendRemindersPageEdit(UserRequest request, int neededPage) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        Pageable pageable = paginationService.formReminderPageableForUser(neededPage, context.userId, userZoneId);
        log.debug("Sending reminders edit page {} for userId={}", neededPage, context.userId);

        EditMessageText pageMessage = ReminderHelper.createRemindersEditMessage(
                userSession,
                timeZoneService,
                reminderRepository,
                pageable,
                context
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
        log.debug("EditMessageText sent for userId={}", context.userId);
    }

    public void sendRemindersFirstPage(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        Pageable pageable = paginationService.formReminderPageableForUser(Pageable.FIRST, context.userId, userZoneId);
        log.debug("Sending first reminders page for userId={}", context.userId);

        SendMessage sendMessage = ReminderHelper.createRemindersMessage(
                userSession,
                timeZoneService,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);
        log.debug("First reminders page sent for userId={}", context.userId);
    }

    public void sendRemindersCurrentPage(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        int currentPage = userSession.getCurrentReminderPage();
        log.debug("Sending current reminders page {} for userId={}", currentPage, context.userId);

        Pageable pageable = paginationService.formReminderPageableForUser(currentPage, context.userId, userZoneId);
        SendMessage remindersMessage = ReminderHelper.createRemindersMessage(
                userSession,
                timeZoneService,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, remindersMessage);
        log.debug("Current reminders page {} sent for userId={}", currentPage, context.userId);
    }

    public void startReminderCreation(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        log.debug("Starting reminder creation for userId={}", context.userId);
        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, ALRIGHT);
        TelegramHelper.sendMessageWithMarkup(
                telegramClient, context.userId, REMINDER_DATE,
                formDateChoiceKeyboard(timeZoneService, context.userId)
        );

        userSession.createReminderDraft();
        userSession.setState(UserState.AWAITING_REMINDER_DATE);
        log.debug("Reminder draft created and state set to AWAITING_REMINDER_DATE for userId={}", context.userId);
    }
}
