package beBig.config;

import beBig.job.AgeUpdateJob;
import beBig.job.AssignDailyMissionJob;
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

    @Bean
    public JobDetail assignDailyMissionJobDetail() {
        return JobBuilder.newJob(AssignDailyMissionJob.class)
                .withIdentity("assignDailyMissionJob")
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

    @Bean
    public CronTrigger assignDailyMissionTrigger(JobDetail assignDailyMissionJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(assignDailyMissionJobDetail)
                .withIdentity("assignDailyMissionTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")) // 매일 0시 0분 0초 실행
                .startNow()
                .build();
    }

    // SchedulerFactoryBean 설정
    @Bean
    public SchedulerFactoryBean schedulerFactory(CronTrigger transactionUpdateTrigger, JobDetail transactionUpdateJobDetail,
                                                 CronTrigger ageUpdateTrigger, JobDetail ageUpdateJobDetail,
                                                 CronTrigger assignDailyMissionTrigger, JobDetail assignDailyMissionJobDetail) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setJobDetails(transactionUpdateJobDetail, ageUpdateJobDetail, assignDailyMissionJobDetail); // JobDetail 등록
        schedulerFactory.setTriggers(transactionUpdateTrigger, ageUpdateTrigger, assignDailyMissionTrigger); // 트리거 등록
        return schedulerFactory;
    }

    @Bean
    public SpringBeanJobFactory jobFactory() {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        return jobFactory;
    }
}
