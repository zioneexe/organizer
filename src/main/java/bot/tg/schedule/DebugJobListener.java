package bot.tg.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DebugJobListener implements JobListener {

    @Override
    public String getName() {
        return "Debug Job Listener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        log.info("Job is about to start: {}", jobExecutionContext.getJobDetail().getKey());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        log.info("Job was vetoed: {}", jobExecutionContext.getJobDetail().getKey());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException jobException) {
        log.info("Job was finished: {}", jobExecutionContext.getJobDetail().getKey());
        if (jobException != null) {
            log.info("Job failed: {}", jobException.getMessage());
        }
    }
}
