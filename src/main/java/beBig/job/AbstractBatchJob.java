package beBig.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class AbstractBatchJob extends QuartzJobBean {
    @Autowired
    private JobLauncher jobLauncher;

    private final Job job;

    protected AbstractBatchJob(Job job) {
        this.job = job;
    }

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            // Spring Batch Job 실행
            jobLauncher.run(job, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters());
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
