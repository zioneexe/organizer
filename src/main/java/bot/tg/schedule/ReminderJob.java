package bot.tg.schedule;

import bot.tg.helper.TelegramHelper;
import bot.tg.repository.ReminderRepository;
import bot.tg.service.TimeZoneService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class ReminderJob implements Job {

    private final TelegramClient telegramClient;
    private final TimeZoneService timeZoneService;
    private final ReminderRepository reminderRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext)  {
        JobDataMap data = jobExecutionContext.getMergedJobDataMap();

        String reminderId = data.getString("reminderId");
        long userId = data.getLong("userId");
        String text = data.getString("text");

        ZoneId userTimeZone = timeZoneService.getUserZoneId(userId);

        reminderRepository.markAsFired(reminderId);

        LocalDateTime reminderTime = reminderRepository.getById(reminderId).getDateTime();
        ZonedDateTime userZonedDateTime = reminderTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(userTimeZone);

        String message = "🔔 Нагадування на " + userZonedDateTime.toLocalTime() + ": " + text;
        TelegramHelper.sendSimpleMessage(telegramClient, userId, message);
    }
}
