package bot.tg;

import bot.tg.service.MessageService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppLifecycleHandler implements CommandLineRunner {

    private final MessageService messageService;

    @Override
    public void run(String... args) {
        System.out.println("✅ Бот Organizer успішно стартанув!");
        scheduleStartupJobs();
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println("⏹️ Бот завершує свою роботу.");
    }

    private void scheduleStartupJobs() {
        messageService.scheduleGreetingsToAll();
        messageService.scheduleUnfiredReminders();
    }
}
