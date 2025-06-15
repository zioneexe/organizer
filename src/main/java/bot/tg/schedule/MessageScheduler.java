package bot.tg.schedule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class MessageScheduler {

    public static void scheduleDailyAt7AM(Runnable task) {
        Timer timer = new Timer();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(7).withMinute(0).withSecond(0);
        if (!now.isBefore(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = Duration.between(now, nextRun).toMillis();
        long oneDay = 24 * 60 * 60 * 1000;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, initialDelay, oneDay);

    }
}
