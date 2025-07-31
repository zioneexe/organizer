package bot.tg.command.impl;

import bot.tg.command.BotCommand;
import bot.tg.dto.ChatContext;
import bot.tg.dto.Pageable;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.state.UserStateManager;
import bot.tg.util.PaginationHelper;
import bot.tg.util.ReminderResponseHelper;
import bot.tg.util.TelegramHelper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

public class RemindersCommand implements BotCommand {

    private final TelegramClient telegramClient;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final UserStateManager userStateManager;

    public RemindersCommand() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.reminderRepository = RepositoryProvider.getReminderRepository();
        this.userRepository = RepositoryProvider.getUserRepository();
        this.userStateManager = ServiceProvider.getUserStateManager();
    }

    @Override
    public void execute(Update update) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = PaginationHelper.formReminderPageableForUser(Pageable.FIRST, userId, userZoneId);
        SendMessage sendMessage = ReminderResponseHelper.createRemindersMessage(
                userStateManager,
                userRepository,
                reminderRepository,
                pageable,
                new ChatContext(userId, chatId)
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        userStateManager.setState(userId, UserState.IDLE);
    }
}
