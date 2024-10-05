package beBig.job;
import beBig.service.HomeService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class QuartzBatchJob extends QuartzJobBean {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private HomeService homeService; // HomeService 주입 추가

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            // HomeService의 메서드를 호출하여 계좌 업데이트
            homeService.updateTransactions(); // HomeService의 메서드 호출
            jobLauncher.run(job, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters());
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}


