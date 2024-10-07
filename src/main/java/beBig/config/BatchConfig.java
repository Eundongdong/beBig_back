package beBig.config;

import beBig.service.HomeService;
import beBig.service.UserService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {

    @Autowired
    private HomeService homeService;

    @Autowired
    private UserService userService;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    // HomeService.updateTransactions()를 수행하는 Step
    @Bean
    public Step transactionUpdateStep() {
        return stepBuilderFactory.get("transactionUpdateStep")
                .tasklet((contribution, chunkContext) -> {
                    homeService.updateTransactions(); // HomeService의 메서드 호출
                    return null;
                }).build();
    }

    // 거래내역 업데이트 Job 정의
    @Bean(name = "transactionUpdateJob")
    public Job transactionUpdateJob() {
        return jobBuilderFactory.get("transactionUpdateJob")
                .start(transactionUpdateStep())
                .build();
    }

    // 사용자 나이 업데이트를 위한 Step
    @Bean
    public Step ageUpdateStep() {
        return stepBuilderFactory.get("ageUpdateStep")
                .tasklet((contribution, chunkContext) -> {
                    userService.updateUserAges(); // UserService의 메서드 호출
                    return null;
                }).build();
    }

    // 나이 업데이트 Job 정의
    @Bean(name = "ageUpdateJob")
    public Job ageUpdateJob() {
        return jobBuilderFactory.get("ageUpdateJob")
                .start(ageUpdateStep())
                .build();
    }
}
