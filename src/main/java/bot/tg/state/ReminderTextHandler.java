package bot.tg.state;

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
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static bot.tg.constant.Reminder.Response.REMINDER_CREATED;

public class ReminderTextHandler implements StateHandler {

    private final TelegramClient telegramClient;
    private final UserStateManager userStateManager;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    public ReminderTextHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userStateManager = ServiceProvider.getUserStateManager();
        this.messageService = ServiceProvider.getMessageService();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
    }

    @Override
    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            String text = update.getMessage().getText();

            ReminderCreateDto dto = userStateManager.getReminderDraft(userId);
            dto.setText(text);

            Reminder reminder = ReminderMapper.fromDto(dto);
            reminderRepository.create(reminder);
            messageService.scheduleReminder(reminder);

            String replyText = REMINDER_CREATED;
            String calendarLink;
            boolean isConnected = userRepository.isGoogleConnected(userId);
            if (isConnected) {
                calendarLink = GoogleCalendarService.createCalendarEvent(userId, dto);
                if (!calendarLink.isBlank()) replyText += "\n\nПодія додана в Google Календар: " + calendarLink;
            }

            userStateManager.setState(userId, UserState.IDLE);
            TelegramHelper.sendSimpleMessage(telegramClient, chatId, replyText);

            SendMessage remindersMessage = ReminderResponseHelper.createRemindersMessage(
                    userRepository,
                    reminderRepository,
                    new ChatContext(userId, chatId)
            );
            TelegramHelper.safeExecute(telegramClient, remindersMessage);
        }
    }
}
