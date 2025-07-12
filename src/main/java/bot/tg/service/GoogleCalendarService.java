package bot.tg.service;

import bot.tg.dto.DateTime;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.format.DateTimeFormatter;

import static bot.tg.schedule.MessageScheduler.DEFAULT_TIMEZONE;

public class GoogleCalendarService {

    private static final String REMINDERS_ID = "primary";

    public static String createCalendarEvent(long userId, ReminderCreateDto reminder) {
        try {
            UserRepository userRepository = RepositoryProvider.getUserRepository();
            String userTimeZone = userRepository.getById(userId).getTimeZone();

            Credential credential = GoogleClientService.getCredentialFromStoredTokens(String.valueOf(userId));
            Calendar calendar = GoogleClientService.getCalendarService(credential);

            Event event = new Event()
                    .setSummary(reminder.getText())
                    .setDescription(reminder.getText());

            String startIso = DateTime.DateTimeMapper.toZonedDateTime(reminder.getDateTime())
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String endIso = DateTime.DateTimeMapper.toZonedDateTime(reminder.getDateTime()).plusMinutes(30)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            EventDateTime start = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(startIso))
                    .setTimeZone(userTimeZone.isEmpty() ? DEFAULT_TIMEZONE : userTimeZone);
            EventDateTime end = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(endIso))
                    .setTimeZone(userTimeZone.isEmpty() ? DEFAULT_TIMEZONE : userTimeZone);

            event.setStart(start).setEnd(end);
            event = calendar.events().insert(REMINDERS_ID, event).execute();

            return event.getHtmlLink();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

}
