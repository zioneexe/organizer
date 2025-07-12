package bot.tg.repository;

import bot.tg.dto.Pageable;
import bot.tg.dto.update.TaskUpdateDto;
import bot.tg.model.TodoTask;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class TaskRepository implements Repository<TodoTask, TaskUpdateDto, String> {

    private static final String COLLECTION_NAME = "tasks";

    private final MongoCollection<TodoTask> tasks;

    public TaskRepository(MongoDatabase database) {
        this.tasks = database.getCollection(COLLECTION_NAME, TodoTask.class);
    }

    public long countByUserForDay(long userId, LocalDate date, ZoneId userZoneId) {
        Date start = Date.from(date.atStartOfDay(userZoneId).toInstant());
        Date end = Date.from(date.plusDays(1).atStartOfDay(userZoneId).toInstant());

        Bson filter = Filters.and(
                Filters.eq("user_id", userId),
                Filters.gte("created_at", start),
                Filters.lt("created_at", end)
        );

        return tasks.countDocuments(filter);
    }

    @Override
    public TodoTask create(TodoTask dto) {
        tasks.insertOne(dto);
        return dto;
    }

    @Override
    public TodoTask getById(String id) {
        return tasks.find(eq("_id", new ObjectId(id))).first();
    }

    public List<TodoTask> getByUserForDayPaged(long userId, Pageable pageable, LocalDate date, ZoneId userZoneId) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPage();
        int skip = pageSize * (pageNumber - 1);

        Date start = Date.from(date.atStartOfDay(userZoneId).toInstant());
        Date end = Date.from(date.plusDays(1).atStartOfDay(userZoneId).toInstant());

        Bson sort = Sorts.ascending("created_at");
        Bson filter = Filters.and(
                Filters.eq("user_id", userId),
                Filters.gte("created_at", start),
                Filters.lt("created_at", end)
        );

        return this.tasks.find(filter).sort(sort).skip(skip).limit(pageSize).into(new ArrayList<>());
    }

    @Override
    public boolean existsById(String id) {
        return tasks.find(eq("_id", new ObjectId(id))).first() != null;
    }

    @Override
    public List<TodoTask> getAll() {
        List<TodoTask> tasks = new ArrayList<>();
        for (TodoTask task : this.tasks.find()) {
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public TodoTask update(String id, TaskUpdateDto dto) {
        Bson filter = eq("_id", new ObjectId(id));

        List<Bson> updates = new ArrayList<>();

        if (dto.getTitle() != null) {
            updates.add(Updates.set("title", dto.getTitle()));
        }

        if (dto.getDescription() != null) {
            updates.add(Updates.set("description", dto.getDescription()));
        }

        if (dto.getStatus() != null) {
            updates.add(Updates.set("completed", dto.getStatus().toBoolean()));
        }

        if (!updates.isEmpty()) {
            updates.add(Updates.set("updated_at", LocalDateTime.now()));
            tasks.updateOne(filter, Updates.combine(updates));
        }

        return getById(id);
    }

    @Override
    public boolean deleteById(String id) {
        DeleteResult result = tasks.deleteOne(eq("_id", new ObjectId(id)));
        return result.getDeletedCount() > 0;
    }
}
