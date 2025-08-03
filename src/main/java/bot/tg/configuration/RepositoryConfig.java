package bot.tg.configuration;

import bot.tg.repository.MongoTokenStore;
import bot.tg.repository.ReminderRepository;
import bot.tg.repository.TaskRepository;
import bot.tg.repository.UserRepository;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public UserRepository userRepository(MongoDatabase database) {
        return new UserRepository(database);
    }

    @Bean
    public TaskRepository taskRepository(MongoDatabase database) {
        return new TaskRepository(database);
    }

    @Bean
    public ReminderRepository reminderRepository(MongoDatabase database) {
        return new ReminderRepository(database);
    }

    @Bean
    public MongoTokenStore tokenStore(MongoDatabase database) {
        return new MongoTokenStore(database);
    }
}
