package beBig.config;

import beBig.job.QuartzBatchJob;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

    // Quartz Job을 정의하는 메서드
    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(QuartzBatchJob.class) // QuartzBatchJob은 실제 실행할 배치 작업
                .withIdentity("batchJob")
                .storeDurably() // 여러 트리거에서 사용 가능하게 함
                .build();
    }

    // 트리거를 정의 (Cron 표현식 사용)
    @Bean
    public CronTrigger trigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("batchJobTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?")) // 5분마다 실행
                .build();
    }

    // SchedulerFactoryBean을 설정하여 Quartz 스케줄러를 등록
    @Bean
    public SchedulerFactoryBean schedulerFactory(Trigger trigger, JobDetail jobDetail) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobDetails(jobDetail);
        schedulerFactory.setTriggers(trigger);
        return schedulerFactory;
    }
}
