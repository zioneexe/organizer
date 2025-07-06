package bot.tg.callback;

import bot.tg.dto.DateTimeDto;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.provider.RepositoryProvider;
import bot.tg.provider.ServiceProvider;
import bot.tg.provider.TelegramClientProvider;
import bot.tg.repository.UserRepository;
import bot.tg.state.UserState;
import bot.tg.util.TelegramHelper;
import bot.tg.util.TimePickerResponseHelper;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static bot.tg.util.Constants.*;

public class ReminderTimePickerHandler implements CallbackHandler {

    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    public ReminderTimePickerHandler() {
        this.telegramClient = TelegramClientProvider.getInstance();
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    @Override
    public boolean supports(String data) {
        return data.startsWith(TIME_PICKER);
    }

    @Override
    public void handle(Update update) {
        if (!update.hasCallbackQuery()) {
            return;
        }

        long userId = update.getCallbackQuery().getFrom().getId();

        String callbackQueryId = update.getCallbackQuery().getId();
        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(COLON_DELIMITER);
        if (parts.length < 2) return;

        String action = parts[1];
        ReminderCreateDto dto = ServiceProvider.getUserStateManager().getReminderDraft(userId);
        DateTimeDto dateTimeDto = dto.getDateTime();

        String userTimeZone = userRepository.getById(userId).getTimeZone();
        ZoneId zoneId = userTimeZone != null && !userTimeZone.isBlank() ?
                ZoneId.of(userTimeZone) :
                ZoneId.systemDefault();

        boolean isToday = dateTimeDto.getDate().isEqual(LocalDate.now(zoneId));
        int currentHour = LocalTime.now(zoneId).getHour();
        int currentMinute = LocalTime.now(zoneId).getMinute();

        switch (action) {
            case CHANGE_HOUR -> {
                int delta = Integer.parseInt(parts[2]);
                int newHour = (dateTimeDto.getHour() + delta + 24) % 24;

                if (isToday && newHour < currentHour) return;
                dateTimeDto.setHour(newHour);
            }
            case CHANGE_MINUTE -> {
                int previousHour = dateTimeDto.getHour();
                int delta = Integer.parseInt(parts[2]);
                int newMinute = (dateTimeDto.getMinute() + delta + 60) % 60;

                if (isToday && previousHour < currentHour) return;
                if (isToday && previousHour == currentHour && newMinute < currentMinute) return;
                dateTimeDto.setMinute(newMinute);
            }
            case CONFIRM -> {
                ServiceProvider.getUserStateManager().setState(userId, UserState.AWAITING_REMINDER_TEXT);
                TelegramHelper.sendSimpleMessage(telegramClient, userId, REMINDER_TEXT);
            }
            case CANCEL -> ServiceProvider.getUserStateManager().clearState(userId);
        }

        EditMessageText editMessage = TimePickerResponseHelper.createTimePickerEditMessage(update);
        TelegramHelper.safeExecute(telegramClient, editMessage);
        TelegramHelper.sendSimpleCallbackAnswer(telegramClient, callbackQueryId);
    }
}
