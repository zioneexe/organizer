import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppLifecycleHandler implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("✅ Бот Organizer успішно стартанув!");
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println("⏹️ Бот завершує свою роботу.");
    }
}
