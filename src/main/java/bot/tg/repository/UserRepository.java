package bot.tg.repository;

import bot.tg.dto.Time;
import bot.tg.dto.update.UserUpdateDto;
import bot.tg.model.User;
import bot.tg.util.Utc;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements Repository<User, UserUpdateDto, Long> {

    private static final String COLLECTION_NAME = "users";

    private final MongoCollection<User> users;

    public UserRepository(MongoDatabase database) {
        this.users = database.getCollection(COLLECTION_NAME, User.class);
    }

    public boolean existsById(Long id) {
        return getById(id) != null;
    }

    public boolean isGoogleConnected(Long id) {
        User user = getById(id);
        return user != null && user.getIsGoogleConnected();
    }

    public boolean greetingsEnabled(Long id) {
        User user = getById(id);
        return user != null && user.getGreetingsEnabled();
    }

    public Time getPreferredGreetingTime(Long id) {
        User user = getById(id);
        return user != null ? user.getPreferredGreetingTime() : null;
    }

    public User getById(Long id) {
        return users.find(Filters.eq("user_id", id)).first();
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        for (User user : this.users.find()) {
            users.add(user);
        }
        return users;
    }

    public String create(User dto) {
        InsertOneResult result = users.insertOne(dto);

        return result.getInsertedId() != null
                ? result.getInsertedId().asObjectId().getValue().toHexString()
                : null;
    }

    public User update(Long id, UserUpdateDto userUpdateDto) {
        Bson filter = Filters.eq("user_id", id);
        Bson update = Updates.combine(
                Updates.set("username", userUpdateDto.getUsername()),
                Updates.set("updated_at", Utc.now())
        );
        users.updateOne(filter, update);

        return getById(id);
    }

    public void setTimeZone(Long id, String timeZone) {
        Bson filter = Filters.eq("user_id", id);
        Bson update = Updates.combine(
                Updates.set("time_zone", timeZone),
                Updates.set("updated_at", Utc.now())
        );

        users.updateOne(filter, update);
    }

    public void setPreferredGreetingTime(Long id, Time preferredGreetingTime) {
        Bson filter = Filters.eq("user_id", id);
        Bson update = Updates.combine(
                Updates.set("preferred_greeting_time", preferredGreetingTime),
                Updates.set("updated_at", Utc.now())
        );

        users.updateOne(filter, update);
    }

    public void setGoogleConnected(Long id, boolean isConnected) {
        Bson filter = Filters.eq("user_id", id);
        Bson update = Updates.combine(
                Updates.set("is_google_connected", isConnected),
                Updates.set("updated_at", Utc.now())
        );

        users.updateOne(filter, update);
    }

    public void setGreetingsEnabled(Long id, boolean isEnabled) {
        Bson filter = Filters.eq("user_id", id);
        Bson update = Updates.combine(
                Updates.set("greetings_enabled", isEnabled),
                Updates.set("updated_at", Utc.now())
        );

        users.updateOne(filter, update);
    }

    public void saveUserLocation(Long userId, double latitude, double longitude) {
        Bson filter = Filters.eq("user_id", userId);
        Document locationEntry = new Document()
                .append("latitude", latitude)
                .append("longitude", longitude)
                .append("timestamp", Instant.now());
        Bson update = Updates.push("location_history", locationEntry);

        users.updateOne(filter, update);
    }

    public boolean deleteById(Long id) {
        DeleteResult result = users.deleteOne(Filters.eq("user_id", id));
        return result.getDeletedCount() > 0;
    }

    public String getCalendarId(Long userId) {
        User user = getById(userId);
        return user != null ? user.getCalendarId() : null;
    }

    public void saveCalendarId(Long userId, String newCalendarId) {
        Bson filter = Filters.eq("user_id", userId);
        Bson update = Updates.set("calendar_id", newCalendarId);

        users.updateOne(filter, update);
    }

}
