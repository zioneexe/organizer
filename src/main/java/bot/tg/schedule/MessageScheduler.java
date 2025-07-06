package bot.tg.schedule;

import bot.tg.model.Reminder;
import bot.tg.model.User;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import static bot.tg.logging.Logger.log;

public class MessageScheduler {

    public static final String DEFAULT_TIMEZONE = "Europe/Kiev";

    private final Scheduler scheduler;

    public MessageScheduler() {
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException("Не вдалося розпочати роботу Quartz", e);
        }
    }

    public void scheduleGoodMorningForUser(User user) {
        String zoneId = user.getTimeZone();
        if (zoneId == null || zoneId.isBlank()) {
            zoneId = TimeZone.getDefault().getID();
        }

        String jobId = "good-morning-" + user.getUserId();
        JobKey jobKey = new JobKey(jobId + "-job", "good-morning");
        TriggerKey triggerKey = new TriggerKey(jobId + "-trigger", "good-morning");

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("userId", user.getUserId());
        dataMap.put("firstName", user.getFirstName());

        JobDetail job = JobBuilder.newJob(GoodMorningJob.class)
                .withIdentity(jobKey)
                .usingJobData(dataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(
                        CronScheduleBuilder.dailyAtHourAndMinute(8, 0)
                                .inTimeZone(TimeZone.getTimeZone(zoneId))
                )
                .build();

        try {
            if (scheduler.checkExists(jobKey)) return;
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule good morning job for user " + user.getUserId(), e);
        }
    }

    public void schedule(Reminder reminder) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("reminderId", reminder.getId());
        dataMap.put("userId", reminder.getUserId());
        dataMap.put("text", reminder.getText());

        String jobId = reminder.getId().toHexString();
        JobKey jobKey = new JobKey("reminder-job-" + jobId, "reminders");
        TriggerKey triggerKey = new TriggerKey("reminder-trigger-" + jobId, "reminders");

        JobDetail job = JobBuilder.newJob(ReminderJob.class)
                .withIdentity(jobKey)
                .usingJobData(dataMap)
                .build();

        LocalDateTime systemDateTime = reminder.getDateTime();
        Instant instant = systemDateTime.atZone(ZoneOffset.systemDefault()).toInstant();
        Date triggerTime = Date.from(instant);

        log("Scheduling reminder id=" + reminder.getId() +
                ", userId=" + reminder.getUserId() +
                ", text=" + reminder.getText() +
                ", reminderDateTime=" + systemDateTime +
                ", triggerTime=" + triggerTime +
                ", now=" + new Date());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(triggerTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        try {
            if (scheduler.checkExists(jobKey)) {
                log("Job already exists for reminder id=" + reminder.getId());
                return;
            }

            scheduler.scheduleJob(job, trigger);
            log("Scheduled job for reminder id=" + reminder.getId() + " successfully.");
        } catch (SchedulerException e) {
            log("Failed to schedule reminder id=" + reminder.getId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to schedule reminder: " + jobId, e);
        }
    }


    public void cancel(Reminder reminder) {
        String jobId = "reminder-job-" + reminder.getId().toHexString();
        try {
            scheduler.deleteJob(new JobKey(jobId, "reminders"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
