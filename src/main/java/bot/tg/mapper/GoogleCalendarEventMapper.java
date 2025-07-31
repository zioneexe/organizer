package bot.tg.mapper;

import bot.tg.model.EventDateTime;
import bot.tg.model.EventReminder;
import bot.tg.model.GoogleCalendarEvent;
import com.google.api.services.calendar.model.Event;

import java.util.List;

public class GoogleCalendarEventMapper {

    public static GoogleCalendarEvent fromDto(Event dto) {
        List<EventReminder> reminders = fromDto(dto.getReminders());
        EventDateTime start = fromDto(dto.getStart());
        EventDateTime end = fromDto(dto.getEnd());

        return new GoogleCalendarEvent(
                dto.getId(),
                dto.getHtmlLink(),
                dto.getSummary(),
                dto.getDescription(),
                start,
                end,
                reminders,
                null,
                null
        );
    }

    private static List<EventReminder> fromDto(Event.Reminders dto) {
        return dto.getOverrides()
                .stream().map((override) ->
                        new EventReminder(
                                override.getMethod(),
                                override.getMinutes()
                        )
                ).toList();
    }

    private static EventDateTime fromDto(com.google.api.services.calendar.model.EventDateTime dto) {
        return new EventDateTime(
                dto.getDateTime().toStringRfc3339(),
                dto.getTimeZone()
        );
    }
}
