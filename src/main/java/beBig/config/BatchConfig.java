package beBig.config;

import beBig.service.HomeService;
import beBig.service.MissionService;
import beBig.service.UserService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {
    private final HomeService homeService;
    private final UserService userService;
    private final MissionService missionService;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, HomeService homeService, UserService userService, MissionService missionService) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.homeService = homeService;
        this.userService = userService;
        this.missionService = missionService;
    }

    @Bean(name = "transactionUpdateJob")
    public Job transactionUpdateJob() {
        return jobBuilderFactory.get("transactionUpdateJob")
                .start(transactionUpdateStep())
                .build();
    }

    @Bean
    public Step transactionUpdateStep() {
        return stepBuilderFactory.get("transactionUpdateStep")
                .tasklet((contribution, chunkContext) -> {
                    homeService.updateTransactions();
                    return null;
                }).build();
    }

    @Bean(name = "ageUpdateJob")
    public Job ageUpdateJob() {
        return jobBuilderFactory.get("ageUpdateJob")
                .start(ageUpdateStep())
                .build();
    }

    @Bean
    public Step ageUpdateStep() {
        return stepBuilderFactory.get("ageUpdateStep")
                .tasklet((contribution, chunkContext) -> {
                    userService.updateUserAges();
                    return null;
                }).build();
    }

    @Bean(name = "assignDailyMissionJob")
    public Job assignDailyMissionJob() {
        return jobBuilderFactory.get("assignDailyMissionJob")
                .start(assignDailyMissionStep())
                .build();
    }

    @Bean
    public Step assignDailyMissionStep() {
        return stepBuilderFactory.get("assignDailyMissionStep")
                .tasklet((contribution, chunkContext) -> {
                    missionService.assignDailyMission();
                    return null;
                }).build();
    }
}

