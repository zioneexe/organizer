package bot.tg.repository;

import bot.tg.dto.Pageable;
import bot.tg.dto.update.ReminderUpdateDto;
import bot.tg.model.GoogleCalendarEvent;
import bot.tg.model.Reminder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class ReminderRepository implements Repository<Reminder, ReminderUpdateDto, String> {

    private static final String COLLECTION_NAME = "reminders";

    private final MongoCollection<Reminder> reminders;

    public ReminderRepository(MongoDatabase database) {
        this.reminders = database.getCollection(COLLECTION_NAME, Reminder.class);
    }

    @Override
    public String create(Reminder dto) {
        InsertOneResult result = reminders.insertOne(dto);

        return result.getInsertedId() != null
                ? result.getInsertedId().asObjectId().getValue().toHexString()
                : null;
    }

    public void attachCalendarEvent(String id, GoogleCalendarEvent googleCalendarEvent) {
        reminders.updateOne(
                Filters.eq("_id", new ObjectId(id)),
                Updates.set("google_calendar_event", googleCalendarEvent)
        );
    }

    public void detachCalendarEvent(String id) {
        reminders.updateOne(
                Filters.eq("_id", new ObjectId(id)),
                Updates.unset("google_calendar_event")
        );
    }

    @Override
    public Reminder getById(String id) {
        return reminders.find(eq("_id", new ObjectId(id))).first();
    }

    public List<Reminder> getUnfiredAfterNow() {
        LocalDateTime now = LocalDateTime.now();
        Bson filter = Filters.and(
                Filters.eq("fired", false),
                Filters.gt("date_time", now)
        );

        return this.reminders.find(filter).into(new ArrayList<>());
    }

    public List<Reminder> getUpcomingForUserPaged(long userId, Pageable pageable, ZoneId userZoneId) {
        LocalDateTime now = LocalDateTime.now(userZoneId);
        ZonedDateTime zonedDateTime = now.atZone(userZoneId);
        Date date = Date.from(zonedDateTime.toInstant());

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPage();
        int skip = pageSize * (pageNumber - 1);

        Bson sort = Sorts.ascending("date_time");
        Bson filter = Filters.and(
                Filters.eq("fired", false),
                Filters.eq("user_id", userId),
                Filters.gt("date_time", date)
        );

        return this.reminders.find(filter).sort(sort).skip(skip).limit(pageSize).into(new ArrayList<>());
    }

    @Override
    public boolean existsById(String id) {
        return reminders.find(eq("_id", new ObjectId(id))).first() != null;
    }

    @Override
    public List<Reminder> getAll() {
        List<Reminder> reminders =  new ArrayList<>();
        for (Reminder reminder : this.reminders.find()) {
            reminders.add(reminder);
        }
        return reminders;
    }

    @Override
    public Reminder update(String id, ReminderUpdateDto dto) {
        Bson filter = eq("_id", new ObjectId(id));
        Bson update = Updates.set("time", dto.getTime());
        reminders.updateOne(filter, update);
        return getById(id);
    }

    @Override
    public boolean deleteById(String id) {
        DeleteResult result = reminders.deleteOne(eq("_id", new ObjectId(id)));
        return result.getDeletedCount() > 0;
    }

    public void markAsFired(String id) {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        Bson update = Updates.combine(
                Updates.set("fired", true),
                Updates.set("updated_at", LocalDateTime.now())
        );

        this.reminders.updateOne(filter, update);
    }

    public void setEnabled(String id, boolean isEnabled) {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        Bson update = Updates.combine(
                Updates.set("enabled", isEnabled),
                Updates.set("updated_at", LocalDateTime.now())
        );

        reminders.updateOne(filter, update);
    }

    public long countUpcomingByUser(long userId, ZoneId userZoneId) {
        LocalDateTime now = LocalDateTime.now(userZoneId);

        Bson filter = Filters.and(
                Filters.eq("fired", false),
                Filters.eq("user_id", userId),
                Filters.gt("date_time", now)
        );

        return reminders.countDocuments(filter);
    }
}
