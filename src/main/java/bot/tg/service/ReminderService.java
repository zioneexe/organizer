package bot.tg.service;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.helper.ReminderResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.ReminderRepository;
import bot.tg.user.UserRequest;
import bot.tg.user.UserSession;
import bot.tg.user.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Response.REMINDER_DATE;
import static bot.tg.constant.ResponseMessage.ALRIGHT;
import static bot.tg.helper.ReminderResponseHelper.formDateChoiceKeyboard;

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
        EditMessageText pageMessage = ReminderResponseHelper.createRemindersEditMessage(
                userSession,
                timeZoneService,
                reminderRepository,
                pageable,
                context
        );
        TelegramHelper.safeExecute(telegramClient, pageMessage);
    }

    public void sendRemindersFirstPage(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        Pageable pageable = paginationService.formReminderPageableForUser(Pageable.FIRST, context.userId, userZoneId);
        SendMessage sendMessage = ReminderResponseHelper.createRemindersMessage(
                userSession,
                timeZoneService,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);
    }

    public void sendRemindersCurrentPage(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        ZoneId userZoneId = timeZoneService.getUserZoneId(context.userId);
        int currentPage = userSession.getCurrentReminderPage();
        Pageable pageable = paginationService.formReminderPageableForUser(currentPage, context.userId, userZoneId);
        SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(
                userSession,
                timeZoneService,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, remindersMessage);
    }

    public void startReminderCreation(UserRequest request) {
        TelegramContext context = request.getContext();
        UserSession userSession = request.getUserSession();

        TelegramHelper.sendMessageWithKeyboardRemove(telegramClient, context.userId, ALRIGHT);
        TelegramHelper.sendMessageWithMarkup(
                telegramClient, context.userId, REMINDER_DATE,
                formDateChoiceKeyboard(timeZoneService, context.userId)
        );

        userSession.createReminderDraft();
        userSession.setState(UserState.AWAITING_REMINDER_DATE);
    }

}
