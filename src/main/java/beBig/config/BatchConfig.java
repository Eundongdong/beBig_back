package beBig.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {

    @Autowired
    private DataSource dataSource;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job exampleJob() {
        return jobBuilderFactory.get("exampleJob")
                .start(exampleStep())
                .build();
    }

    @Bean
    public Step exampleStep() {
        return stepBuilderFactory.get("exampleStep")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Spring Batch is running");
                    return null;
                }).build();
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        // jdbc로 설정한 DataSource 주입
        super.setDataSource(dataSource);
    }
}

