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

import java.util.Properties;
import java.util.TimeZone;

@Configuration
public class QuartzConfig {

    @Autowired
    private SpringBeanJobFactory jobFactory;

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

    @Bean
    public CronTrigger transactionUpdateTrigger(JobDetail transactionUpdateJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(transactionUpdateJobDetail)
                .withIdentity("transactionUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 12 * * ?"))
                .startNow()
                .build();
    }

    @Bean
    public CronTrigger ageUpdateTrigger(JobDetail ageUpdateJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(ageUpdateJobDetail)
                .withIdentity("ageUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 1 ?"))
                .startNow()
                .build();
    }

    @Bean
    public CronTrigger assignDailyMissionTrigger(JobDetail assignDailyMissionJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(assignDailyMissionJobDetail)
                .withIdentity("assignDailyMissionTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))
                .startNow()
                .build();
    }

    @Bean
    public CronTrigger updateMonthlyMissionTrigger(JobDetail updateMonthlyMissionJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(updateMonthlyMissionJobDetail)
                .withIdentity("updateMonthlyMissionTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?")) // 매달 1일에 실행
                .startNow()
                .build();
    }

    @Bean
    public CronTrigger dailyCheckMonthlyMissionsTrigger(JobDetail dailyCheckMonthlyMissionsJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(dailyCheckMonthlyMissionsJobDetail)
                .withIdentity("dailyCheckMonthlyMissionsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 50 23 * * ?")) // 매일 23시 50분에 실행 -> 더 빈번하게 해도 될거같음
                .startNow()
                .build();
    }

    @Bean
    public CronTrigger checkEndOfMonthMissionsTrigger(JobDetail checkEndOfMonthMissionsJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(checkEndOfMonthMissionsJobDetail)
                .withIdentity("checkEndOfMonthMissionsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 50 23 L * ?")) // 매달 마지막날 23:50에 실행
                .startNow()
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
                updateMonthlyMissionTrigger,dailyCheckMonthlyMissionsTrigger, checkEndOfMonthMissionsTrigger);

        // 한국 표준시(KRT) 설정
        Properties quartzProperties = new Properties();
        quartzProperties.setProperty("org.quartz.scheduler.timeZone", TimeZone.getTimeZone("Asia/Seoul").getID());
        schedulerFactory.setQuartzProperties(quartzProperties);

        return schedulerFactory;
    }

    @Bean
    public SpringBeanJobFactory jobFactory() {
        return new SpringBeanJobFactory();
    }
}
