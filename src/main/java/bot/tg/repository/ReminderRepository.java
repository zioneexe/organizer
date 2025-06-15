package bot.tg.repository;

import bot.tg.dto.update.ReminderUpdateDto;
import bot.tg.model.Reminder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ReminderRepository implements Repository<Reminder, ReminderUpdateDto, String> {

    private static final String COLLECTION_NAME = "reminders";

    private final MongoCollection<Reminder> reminders;

    public ReminderRepository(MongoDatabase database) {
        this.reminders = database.getCollection(COLLECTION_NAME, Reminder.class);
    }

    @Override
    public Reminder create(Reminder dto) {
        reminders.insertOne(dto);
        return dto;
    }

    @Override
    public Reminder getById(String id) {
        return reminders.find(Filters.eq("_id", new ObjectId(id))).first();
    }

    @Override
    public boolean existsById(String id) {
        return reminders.find(Filters.eq("_id", new ObjectId(id))).first() != null;
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
        Bson filter = Filters.eq("_id", new ObjectId(id));
        Bson update = Updates.set("time", dto.getTime());
        reminders.updateOne(filter, update);
        return getById(id);
    }

    @Override
    public boolean deleteById(String id) {
        DeleteResult result = reminders.deleteOne(Filters.eq("_id", new ObjectId(id)));
        return result.getDeletedCount() > 0;
    }
}
