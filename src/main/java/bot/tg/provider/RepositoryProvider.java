package bot.tg.provider;

import bot.tg.repository.ReminderRepository;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

public class RepositoryProvider {

    @Getter
    private static TaskRepository taskRepository;
    @Getter
    private static UserRepository userRepository;
    @Getter
    private static ReminderRepository reminderRepository;

    public static void init(MongoDatabase database) {
        RepositoryProvider.userRepository = new UserRepository(database);
        RepositoryProvider.taskRepository = new TaskRepository(database);
        RepositoryProvider.reminderRepository = new ReminderRepository(database);
    }

}
