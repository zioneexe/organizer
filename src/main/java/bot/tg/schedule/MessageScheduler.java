package bot.tg.schedule;

import bot.tg.model.Reminder;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Timestamp;

public class MessageScheduler {

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

    public void scheduleGoodMorningToAll() {
        JobKey jobKey = new JobKey("good-morning-job", "system");
        TriggerKey triggerKey = new TriggerKey("good-morning-trigger", "system");

        JobDetail job = JobBuilder.newJob(GoodMorningJob.class)
                .withIdentity(jobKey)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(8, 0))
                .build();

        try {
            if (scheduler.checkExists(jobKey)) return;
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule good morning job: " + e);
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

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(Timestamp.valueOf(reminder.getDateTime()))
                .build();
        try {
            if (scheduler.checkExists(jobKey)) return;
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
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
