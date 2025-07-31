package bot.tg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCalendarEvent {

    private String id;

    private String htmlLink;

    private String summary;

    private String description;

    private EventDateTime start;

    private EventDateTime end;

    private List<EventReminder> reminders;

    @BsonProperty("attached_at")
    private LocalDateTime attachedAt;

    @BsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
