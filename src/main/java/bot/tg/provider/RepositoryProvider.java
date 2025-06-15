package bot.tg.provider;

import bot.tg.repository.ReminderRepository;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

public class RepositoryProvider {

    private static RepositoryProvider instance;

    @Getter
    private final TaskRepository taskRepository;
    @Getter
    private final UserRepository userRepository;
    @Getter
    private final ReminderRepository reminderRepository;

    private RepositoryProvider(MongoDatabase database) {
        this.userRepository = new UserRepository(database);
        this.taskRepository = new TaskRepository(database);
        this.reminderRepository = new ReminderRepository(database);
    }

    public static void init(MongoDatabase database) {
        if (instance == null) {
            instance = new RepositoryProvider(database);
        }
    }

    public static RepositoryProvider getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Repository provider not initialized");
        }
        return instance;
    }

}
