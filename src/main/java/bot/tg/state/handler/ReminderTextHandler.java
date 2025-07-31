package bot.tg.state.handler;

import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.mapper.ReminderMapper;
import bot.tg.model.Reminder;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.GoogleCalendarService;
import bot.tg.service.MessageService;
import bot.tg.state.StateHandler;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.PaginationHelper;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

import static bot.tg.constant.Reminder.Response.REMINDER_CREATED;

public class ReminderTextHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final GoogleCalendarService googleCalendarService;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    public ReminderTextHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.messageService = ServiceProvider.getMessageService();
        this.googleCalendarService = ServiceProvider.getGoogleCalendarService();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
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

            userStateManager.setState(userId, UserState.IDLE);

            ReminderCreateDto dto = userStateManager.getReminderDraft(userId);
            dto.setText(text);

            Reminder reminder = ReminderMapper.fromDto(dto);
            String reminderId = reminderRepository.create(reminder);
            messageService.scheduleReminder(reminder);

            StringBuilder replyTextBuilder = new StringBuilder(REMINDER_CREATED);
            boolean isConnected = userRepository.isGoogleConnected(userId);
            if (isConnected) {
                this.googleCalendarService.createCalendarEventAndReturnLink(userId, reminderId, dto)
                        .ifPresent(calendarLink -> replyTextBuilder
                                .append("\n\nПодія додана в Google Календар: ")
                                .append(calendarLink));
            }

            TelegramHelper.sendSimpleMessage(telegramClient, chatId, replyTextBuilder.toString());

            SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(
                    userRepository,
                    reminderRepository,
                    new ChatContext(userId, chatId)
            );
            TelegramHelper.safeExecute(telegramClient, remindersMessage);
        }
    }
}
