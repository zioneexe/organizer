package bot.tg.service;

import bot.tg.dto.DateTime;
import bot.tg.dto.SupportedTimeZone;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.mapper.GoogleCalendarEventMapper;
import bot.tg.model.GoogleCalendarEvent;
import bot.tg.model.Reminder;
import bot.tg.provider.RepositoryProvider;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GoogleCalendarService {

    private static final String REMINDERS_ID = "primary";

    private static final String BOT_URL = System.getenv("BOT_URL");

    private final Map<Long, Calendar> userCalendars = new ConcurrentHashMap<>();
    private final ReminderRepository reminderRepository;

    public GoogleCalendarService() {
        this.reminderRepository = RepositoryProvider.getReminderRepository();
    }

    public Optional<String> createCalendarEventAndReturnLink(long userId, String reminderId, ReminderCreateDto reminder) {
        try {
            Event event = buildCalendarEvent(userId, reminder);
            Calendar calendar = getCalendarForUser(userId);
            event = calendar.events()
                    .insert(REMINDERS_ID, event)
                    .execute();

            GoogleCalendarEvent googleCalendarEvent = GoogleCalendarEventMapper.fromDto(event);
            googleCalendarEvent.setAttachedAt(LocalDateTime.now());
            reminderRepository.attachCalendarEvent(reminderId, googleCalendarEvent);

            return Optional.of(event.getHtmlLink());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void deleteCalendarEvent(long userId, String reminderId) {
        try {
            Calendar calendar = getCalendarForUser(userId);

            Reminder reminder = reminderRepository.getById(reminderId);
            if (reminder == null || reminder.getGoogleCalendarEvent() == null) {
                return;
            }

            String eventId = reminder.getGoogleCalendarEvent().getId();
            calendar.events()
                    .delete(REMINDERS_ID, eventId)
                    .execute();

            reminderRepository.detachCalendarEvent(reminderId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Event buildCalendarEvent(long userId, ReminderCreateDto reminder) {
        String defaultZone = SupportedTimeZone.getDefault().getZoneId();

        UserRepository userRepository = RepositoryProvider.getUserRepository();
        String userTimeZone = userRepository.getById(userId).getTimeZone();

        Event event = new Event()
                .setSummary(reminder.getText())
                .setDescription("\uD83D\uDD14 Created by Organizer Telegram Bot")
                .setTransparency("transparent");

        String startIso = DateTime.DateTimeMapper.toZonedDateTime(reminder.getDateTime())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String endIso = DateTime.DateTimeMapper.toZonedDateTime(reminder.getDateTime()).plusMinutes(30)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startIso))
                .setTimeZone(userTimeZone.isEmpty() ? defaultZone : userTimeZone);
        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(endIso))
                .setTimeZone(userTimeZone.isEmpty() ? defaultZone : userTimeZone);

        event.setColorId("4");

        event.setStart(start).setEnd(end);
        event.setSource(new Event.Source()
                .setTitle("Telegram Reminder Bot")
                .setUrl(BOT_URL)
        );

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(List.of(new EventReminder()
                                .setMethod("popup")
                                .setMinutes(10)
                ));
        event.setReminders(reminders);

        return event;
    }

    private Calendar getCalendarForUser(long userId) throws Exception {
        if (!userCalendars.containsKey(userId)) {
            Credential credential = GoogleClientService.getCredentialFromStoredTokens(String.valueOf(userId));
            Calendar calendar = GoogleClientService.getCalendarService(credential);

            userCalendars.put(userId, calendar);
        }

        return userCalendars.get(userId);
    }

}
