package bot.tg.repository;

import bot.tg.dto.update.UserUpdateDto;
import bot.tg.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserRepository implements Repository<User, UserUpdateDto, Long> {

    private static final String COLLECTION_NAME = "users";

    private final MongoCollection<User> users;

    public UserRepository(MongoDatabase database) {
        this.users = database.getCollection(COLLECTION_NAME, User.class);
    }

    public User create(User dto) {
        users.insertOne(dto);
        return dto;
    }

    public User getById(Long id) {
        return users.find(Filters.eq("user_id", id)).first();
    }

    public List<User> getByIds(Set<Long> userIds) {
        return users.find(Filters.in("user_id", userIds)).into(new ArrayList<>());
    }

    public boolean existsById(Long id) {
        return users.find(Filters.eq("user_id", id)).first() != null;
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        for (User user : this.users.find()) {
            users.add(user);
        }
        return users;
    }

    public User update(Long id, UserUpdateDto userUpdateDto) {
        Bson filter = Filters.eq("user_id", id);
        Bson update = Updates.set("username", userUpdateDto.getUsername());
        users.updateOne(filter, update);
        return getById(id);
    }

    public boolean isGoogleConnected(Long id) {
        Bson filter = Filters.eq("user_id", id);
        User user = users.find(filter).first();
        return user != null && user.getIsGoogleConnected();
    }

    public void markAsGoogleConnected(Long id, boolean isConnected) {
        Bson filter = Filters.eq("user_id", id);
        Bson update = Updates.combine(
                Updates.set("is_google_connected", isConnected),
                Updates.set("updated_at", LocalDateTime.now())
        );

        users.updateOne(filter, update);
    }

    public boolean deleteById(Long id) {
        DeleteResult result = users.deleteOne(Filters.eq("user_id", id));
        return result.getDeletedCount() > 0;
    }
}
