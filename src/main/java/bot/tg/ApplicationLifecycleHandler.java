package bot.tg;

import bot.tg.service.MessageService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static bot.tg.constant.Core.BOT_STARTED;
import static bot.tg.constant.Core.BOT_STOPPED;

@Component
@RequiredArgsConstructor
public class ApplicationLifecycleHandler implements CommandLineRunner {

    private final MessageService messageService;

    @Override
    public void run(String... args) {
        System.out.println(BOT_STARTED);
        scheduleStartupJobs();
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println(BOT_STOPPED);
    }

    private void scheduleStartupJobs() {
        messageService.scheduleGreetingsToAll();
        messageService.scheduleUnfiredReminders();
    }
}
