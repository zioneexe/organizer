package bot.tg.handler.command.impl;

import bot.tg.dto.Pageable;
import bot.tg.dto.TelegramContext;
import bot.tg.handler.command.BotCommand;
import bot.tg.helper.ReminderResponseHelper;
import bot.tg.helper.TelegramHelper;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import bot.tg.service.PaginationService;
import bot.tg.user.UserRequest;
import bot.tg.user.UserState;
import bot.tg.user.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class RemindersCommand extends BotCommand {

    private final TelegramClient telegramClient;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final UserStateManager userStateManager;
    private final PaginationService paginationService;

    @Override
    public String getCommand() {
        return "/reminders";
    }

    @Override
    public void handle(UserRequest request) {
        TelegramContext context = request.getContext();

        String userTimeZone = userRepository.getById(context.userId).getTimeZone();
        ZoneId userZoneId = userTimeZone == null || userTimeZone.isBlank() ?
                ZoneId.systemDefault() :
                ZoneId.of(userTimeZone);

        Pageable pageable = paginationService.formReminderPageableForUser(Pageable.FIRST, context.userId, userZoneId);
        SendMessage sendMessage = ReminderResponseHelper.createRemindersMessage(
                userStateManager,
                userRepository,
                reminderRepository,
                pageable,
                context.userId
        );
        TelegramHelper.safeExecute(telegramClient, sendMessage);

        userStateManager.setState(context.userId, UserState.IDLE);
    }
}
