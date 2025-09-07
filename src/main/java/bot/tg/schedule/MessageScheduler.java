package bot.tg.schedule;

import bot.tg.dto.Time;
import bot.tg.model.Reminder;
import bot.tg.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageScheduler {

    private final Scheduler scheduler;

    public void cancelGreetingForUser(User user) {
        long userId = user.getUserId();

        String jobId = "greeting-" + userId;
        JobKey jobKey = new JobKey(jobId + "-job", "greeting");

        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to unschedule greeting job for user " + userId, e);
        }
    }

    public void scheduleGreetingForUser(User user) {
        String zoneId = user.getTimeZone();
        if (zoneId == null || zoneId.isBlank()) {
            zoneId = TimeZone.getDefault().getID();
        }

        Time time = user.getPreferredGreetingTime();

        String jobId = "greeting-" + user.getUserId();
        JobKey jobKey = new JobKey(jobId + "-job", "greeting");
        TriggerKey triggerKey = new TriggerKey(jobId + "-trigger", "greeting");

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("userId", user.getUserId());
        dataMap.put("firstName", user.getFirstName());

        JobDetail job = JobBuilder.newJob(GreetingJob.class)
                .withIdentity(jobKey)
                .usingJobData(dataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(
                        CronScheduleBuilder.dailyAtHourAndMinute(time.getHour(), time.getMinute())
                                .inTimeZone(TimeZone.getTimeZone(zoneId))
                )
                .build();

        try {
            if (scheduler.checkExists(jobKey)) return;
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule greeting job for user " + user.getUserId(), e);
        }
    }

    public void schedulePillsReminderInKyiv(long userId, List<Time> reminderTimes) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("userId", userId);

        JobKey jobKey = new JobKey("pills-reminder-job-" + userId, "pills-reminder");

        JobDetail job = JobBuilder.newJob(PillsReminderJob.class)
                .withIdentity(jobKey)
                .usingJobData(dataMap)
                .build();

        Set<Trigger> triggers = new HashSet<>();

        for (int i = 0; i < reminderTimes.size(); i++) {
            Time time = reminderTimes.get(i);

            TriggerKey triggerKey = new TriggerKey("pills-reminder-trigger-" + userId + "-" + i, "reminders");

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(time.getHour(), time.getMinute())
                            .inTimeZone(TimeZone.getTimeZone("Europe/Kyiv"))
                            .withMisfireHandlingInstructionFireAndProceed())
                    .build();

            triggers.add(trigger);
        }

        try {
            if (scheduler.checkExists(jobKey)) {
                log.info("Job already exists for user {}", userId);
                return;
            }

            scheduler.scheduleJob(job, triggers, true);
            log.info("Scheduled job with {} triggers for user {}", triggers.size(), userId);
        } catch (SchedulerException e) {
            log.error("Failed to schedule pills reminder job for user {}. {}",  userId, e.getMessage());
            throw new RuntimeException("Failed to schedule pills reminder for user: " + userId, e);
        }
    }

    public void scheduleReminder(Reminder reminder) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("reminderId", reminder.getId().toHexString());
        dataMap.put("userId", reminder.getUserId());
        dataMap.put("text", reminder.getText());

        String jobId = reminder.getId().toHexString();
        JobKey jobKey = new JobKey("reminder-job-" + jobId, "reminders");
        TriggerKey triggerKey = new TriggerKey("reminder-trigger-" + jobId, "reminders");

        JobDetail job = JobBuilder.newJob(ReminderJob.class)
                .withIdentity(jobKey)
                .usingJobData(dataMap)
                .build();

        LocalDateTime utcDateTime = reminder.getDateTime();

        Instant reminderInstant = utcDateTime.atZone(ZoneOffset.UTC).toInstant();
        Instant nowInstant = Instant.now();

        if (!reminderInstant.isAfter(nowInstant)) {
            reminderInstant = nowInstant.plusSeconds(10);
        }

        Date triggerTime = Date.from(reminderInstant);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(triggerTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        try {
            if (scheduler.checkExists(jobKey)) {
                log.info("Job already exists for reminder id={}", reminder.getId());
                return;
            }

            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled job for reminder id={} successfully.", reminder.getId());
        } catch (SchedulerException e) {
            log.error("Failed to schedule reminder id={}: {}", reminder.getId(), e.getMessage());
            throw new RuntimeException("Failed to schedule reminder: " + jobId, e);
        }
    }

    public void cancelReminder(Reminder reminder) {
        String jobId = "reminder-job-" + reminder.getId().toHexString();
        try {
            scheduler.deleteJob(new JobKey(jobId, "reminders"));
        } catch (SchedulerException e) {
            log.error("Failed to cancel reminder id={}: {}", jobId, e.getMessage());
        }
    }
}
