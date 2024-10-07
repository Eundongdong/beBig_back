package beBig.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class AgeUpdateJob extends QuartzJobBean {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("ageUpdateJob")
    private Job ageUpdateJob; // BatchConfig에서 정의한 Job 주입

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            // Spring Batch Job 실행
            jobLauncher.run(ageUpdateJob, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters());
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
