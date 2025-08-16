package bot.tg.service;

import bot.tg.dto.DateTime;
import bot.tg.dto.SupportedTimeZone;
import bot.tg.dto.create.ReminderCreateDto;
import bot.tg.mapper.GoogleCalendarEventMapper;
import bot.tg.model.GoogleCalendarEvent;
import bot.tg.model.Reminder;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static bot.tg.constant.Core.DEFAULT_CALENDAR_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private static final String BOT_TITLE = "Organizer Telegram Bot";
    private static final String EVENT_TITLE = "Telegram Bot Reminder";
    private static final String EVENT_DESCRIPTION = "\uD83D\uDD14 Created by Organizer Telegram Bot";
    private static final String EVENT_COLOR_ID = "4";

    private final String botUrl;

    private final Map<Long, Calendar> userCalendars = new ConcurrentHashMap<>();
    private final GoogleClientService googleClientService;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final TimeZoneService timeZoneService;

    public Optional<String> createCalendarEventAndReturnLink(Long userId, String reminderId, ReminderCreateDto reminder) {
        try {
            Event event = buildCalendarEvent(userId, reminder);
            Calendar calendar = getCalendarForUser(userId);
            String calendarId = getUserCalendarId(userId, calendar);

            event = calendar.events()
                    .insert(calendarId, event)
                    .execute();

            GoogleCalendarEvent googleCalendarEvent = GoogleCalendarEventMapper.fromDto(event);
            googleCalendarEvent.setAttachedAt(LocalDateTime.now(ZoneOffset.UTC));
            reminderRepository.attachCalendarEvent(reminderId, googleCalendarEvent);

            return Optional.of(event.getHtmlLink());
        } catch (Exception e) {
            log.error("Error in creating calendar event: {}", e.getMessage());
        }

        return Optional.empty();
    }

    public void deleteCalendarEvent(Long userId, String reminderId) {
        try {
            Calendar calendar = getCalendarForUser(userId);
            String calendarId = getUserCalendarId(userId, calendar);

            Reminder reminder = reminderRepository.getById(reminderId);
            if (reminder == null || reminder.getGoogleCalendarEvent() == null) {
                return;
            }

            String eventId = reminder.getGoogleCalendarEvent().getId();
            calendar.events()
                    .delete(calendarId, eventId)
                    .execute();

            reminderRepository.detachCalendarEvent(reminderId);
        } catch (Exception e) {
            log.error("Error in deleting calendar event: {}", e.getMessage());
        }
    }

    private Event buildCalendarEvent(long userId, ReminderCreateDto reminder) {
        String defaultZone = SupportedTimeZone.getDefault().getZoneId();

        String userTimeZone = userRepository.getById(userId).getTimeZone();

        Event event = new Event()
                .setSummary(reminder.getText())
                .setDescription(EVENT_DESCRIPTION)
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

        event.setColorId(EVENT_COLOR_ID);

        event.setStart(start).setEnd(end);
        event.setSource(new Event.Source()
                .setTitle(EVENT_TITLE)
                .setUrl(botUrl)
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

    private Calendar getCalendarForUser(Long userId) throws Exception {
        if (!userCalendars.containsKey(userId)) {
            Credential credential = googleClientService.getCredentialFromStoredTokens(String.valueOf(userId));
            Calendar calendar = googleClientService.getCalendarService(credential);

            userCalendars.put(userId, calendar);
        }

        return userCalendars.get(userId);
    }

    private String getUserCalendarId(Long userId, Calendar calendarService) throws IOException {
        String existingCalendarId = userRepository.getCalendarId(userId);
        if (existingCalendarId != null && !existingCalendarId.isBlank() && !existingCalendarId.equals(DEFAULT_CALENDAR_ID)) {
            return existingCalendarId;
        }

        String userTimeZone = timeZoneService.getUserZoneId(userId).getId();

        com.google.api.services.calendar.model.Calendar newCalendar =
                new com.google.api.services.calendar.model.Calendar()
                        .setSummary(BOT_TITLE)
                        .setTimeZone(userTimeZone);

        String newCalendarId = calendarService.calendars()
                .insert(newCalendar)
                .execute()
                .getId();

        userRepository.saveCalendarId(userId, newCalendarId);
        return newCalendarId;
    }

}
