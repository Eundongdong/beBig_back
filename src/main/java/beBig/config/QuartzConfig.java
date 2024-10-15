package beBig.config;

import beBig.job.*;
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

import java.util.TimeZone;

@Configuration
public class QuartzConfig {

    @Autowired
    private SpringBeanJobFactory jobFactory;

    // 한국 표준시 TimeZone Bean 설정
    @Bean
    public TimeZone krTimeZone() {
        return TimeZone.getTimeZone("Asia/Seoul"); // GMT+9
    }

    @Bean
    public JobDetail transactionUpdateJobDetail() {
        return JobBuilder.newJob(TransactionUpdateJob.class)
                .withIdentity("transactionUpdateJob")
                .storeDurably()
                .build();
    }

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

    @Bean
    public JobDetail updateMonthlyMissionJobDetail() {
        return JobBuilder.newJob(UpdateMonthlyMissionJob.class)
                .withIdentity("updateMonthlyMissionJob")
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail dailyCheckMonthlyMissionsJobDetail() {
        return JobBuilder.newJob(DailyCheckMonthlyMissionsJob.class)
                .withIdentity("dailyCheckMonthlyMissionsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail checkEndOfMonthMissionsJobDetail() {
        return JobBuilder.newJob(CheckEndOfMonthMissionsJob.class)
                .withIdentity("checkEndOfMonthMissionsJob")
                .storeDurably()
                .build();
    }

    // 트리거에 TimeZone 적용
    @Bean
    public CronTrigger transactionUpdateTrigger(JobDetail transactionUpdateJobDetail, TimeZone krTimeZone) {
        return TriggerBuilder.newTrigger()
                .forJob(transactionUpdateJobDetail)
                .withIdentity("transactionUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 23/8 * * ?")
                        .inTimeZone(krTimeZone))  // TimeZone 적용
                .build();
    }

    @Bean
    public CronTrigger ageUpdateTrigger(JobDetail ageUpdateJobDetail, TimeZone krTimeZone) {
        return TriggerBuilder.newTrigger()
                .forJob(ageUpdateJobDetail)
                .withIdentity("ageUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 1 ?")
                        .inTimeZone(krTimeZone))  // TimeZone 적용
                .build();
    }

    @Bean
    public CronTrigger assignDailyMissionTrigger(JobDetail assignDailyMissionJobDetail, TimeZone krTimeZone) {
        return TriggerBuilder.newTrigger()
                .forJob(assignDailyMissionJobDetail)
                .withIdentity("assignDailyMissionTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")
                        .inTimeZone(krTimeZone))  // TimeZone 적용
                .build();
    }

    @Bean
    public CronTrigger updateMonthlyMissionTrigger(JobDetail updateMonthlyMissionJobDetail, TimeZone krTimeZone) {
        return TriggerBuilder.newTrigger()
                .forJob(updateMonthlyMissionJobDetail)
                .withIdentity("updateMonthlyMissionTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 * ?")
                        .inTimeZone(krTimeZone))  // TimeZone 적용
                .build();
    }

    @Bean
    public CronTrigger dailyCheckMonthlyMissionsTrigger(JobDetail dailyCheckMonthlyMissionsJobDetail, TimeZone krTimeZone) {
        return TriggerBuilder.newTrigger()
                .forJob(dailyCheckMonthlyMissionsJobDetail)
                .withIdentity("dailyCheckMonthlyMissionsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 50 23 * * ?")
                        .inTimeZone(krTimeZone))  // TimeZone 적용
                .build();
    }

    @Bean
    public CronTrigger checkEndOfMonthMissionsTrigger(JobDetail checkEndOfMonthMissionsJobDetail, TimeZone krTimeZone) {
        return TriggerBuilder.newTrigger()
                .forJob(checkEndOfMonthMissionsJobDetail)
                .withIdentity("checkEndOfMonthMissionsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 50 23 L * ?")
                        .inTimeZone(krTimeZone))  // TimeZone 적용
                .build();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactory(CronTrigger transactionUpdateTrigger, JobDetail transactionUpdateJobDetail,
                                                 CronTrigger ageUpdateTrigger, JobDetail ageUpdateJobDetail,
                                                 CronTrigger assignDailyMissionTrigger, JobDetail assignDailyMissionJobDetail,
                                                 CronTrigger updateMonthlyMissionTrigger, JobDetail updateMonthlyMissionJobDetail,
                                                 CronTrigger dailyCheckMonthlyMissionsTrigger, JobDetail dailyCheckMonthlyMissionsJobDetail,
                                                 CronTrigger checkEndOfMonthMissionsTrigger, JobDetail checkEndOfMonthMissionsJobDetail) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setJobDetails(transactionUpdateJobDetail, ageUpdateJobDetail, assignDailyMissionJobDetail,
                updateMonthlyMissionJobDetail, dailyCheckMonthlyMissionsJobDetail, checkEndOfMonthMissionsJobDetail);
        schedulerFactory.setTriggers(transactionUpdateTrigger, ageUpdateTrigger, assignDailyMissionTrigger,
                updateMonthlyMissionTrigger, dailyCheckMonthlyMissionsTrigger, checkEndOfMonthMissionsTrigger);

        return schedulerFactory;
    }

    @Bean
    public SpringBeanJobFactory jobFactory() {
        return new SpringBeanJobFactory();
    }
}

