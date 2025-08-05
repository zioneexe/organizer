package bot.tg.configuration;

import bot.tg.util.validation.TaskAndReminderValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class AppConfig {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TelegramClient telegramClient(@Value("${TELEGRAM_BOT_API_KEY}") String apiKey) {
        return new OkHttpTelegramClient(apiKey);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TaskAndReminderValidator validator() {
        return new TaskAndReminderValidator();
    }
}
