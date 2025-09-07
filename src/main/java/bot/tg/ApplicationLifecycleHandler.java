package bot.tg;

import bot.tg.service.MessageService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static bot.tg.constant.Core.Message.BOT_STARTED;
import static bot.tg.constant.Core.Message.BOT_STOPPED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationLifecycleHandler implements CommandLineRunner {

    private final MessageService messageService;

    @Override
    public void run(String... args) {
        log.info(BOT_STARTED);
        scheduleStartupJobs();
    }

    @PreDestroy
    public void onShutdown() {
        log.info(BOT_STOPPED);
    }

    private void scheduleStartupJobs() {
        log.info("Scheduling startup jobs.");
        messageService.scheduleGreetingsToAll();
        messageService.scheduleUnfiredReminders();
        messageService.schedulePillsReminderForUser();
    }
}
