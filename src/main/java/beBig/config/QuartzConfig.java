package beBig.config;

import beBig.job.AgeUpdateJob;
import beBig.job.TransactionUpdateJob;
import org.quartz.JobDetail;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class QuartzConfig {

    @Autowired
    private SpringBeanJobFactory jobFactory;

    // 거래내역 업데이트 JobDetail 정의
    @Bean
    public JobDetail transactionUpdateJobDetail() {
        return JobBuilder.newJob(TransactionUpdateJob.class)
                .withIdentity("transactionUpdateJob")
                .storeDurably()
                .build();
    }

    // 나이 업데이트 JobDetail 정의
    @Bean
    public JobDetail ageUpdateJobDetail() {
        return JobBuilder.newJob(AgeUpdateJob.class)
                .withIdentity("ageUpdateJob")
                .storeDurably()
                .build();
    }

    // 거래내역 업데이트 트리거 정의
    @Bean
    public CronTrigger transactionUpdateTrigger(JobDetail transactionUpdateJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(transactionUpdateJobDetail)
                .withIdentity("transactionUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0/6 * * ?")) // 6시간마다 실행(0시부터 6시간마다)
                .startNow()
                .build();
    }

    // 나이 업데이트 트리거 정의
    @Bean
    public CronTrigger ageUpdateTrigger(JobDetail ageUpdateJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(ageUpdateJobDetail)
                .withIdentity("ageUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 1 ?")) // 매년 1월 1일 자정 실행
                .startNow()
                .build();
    }

    // SchedulerFactoryBean 설정
    @Bean
    public SchedulerFactoryBean schedulerFactory(CronTrigger transactionUpdateTrigger, JobDetail transactionUpdateJobDetail,
                                                 CronTrigger ageUpdateTrigger, JobDetail ageUpdateJobDetail) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setJobDetails(transactionUpdateJobDetail, ageUpdateJobDetail); // 두 JobDetail 등록
        schedulerFactory.setTriggers(transactionUpdateTrigger, ageUpdateTrigger); // 두 트리거 등록
        return schedulerFactory;
    }

    @Bean
    public SpringBeanJobFactory jobFactory() {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        return jobFactory;
    }
}
