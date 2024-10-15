package beBig.config;

import beBig.mapper.AccountMapper;
import beBig.service.HomeService;
import beBig.service.MissionService;
import beBig.service.UserService;
import beBig.vo.AccountVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {
    private final HomeService homeService;
    private final UserService userService;
    private final MissionService missionService;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final AccountMapper accountMapper;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, HomeService homeService, UserService userService, MissionService missionService, AccountMapper accountMapper) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.homeService = homeService;
        this.userService = userService;
        this.missionService = missionService;
        this.accountMapper = accountMapper;
    }

    @Bean
    public Job transactionUpdateJob() {
        return jobBuilderFactory.get("transactionUpdateJob")
                .incrementer(new RunIdIncrementer())
                .flow(transactionUpdateStep())
                .end()
                .build();
    }

    @Bean
    public Step transactionUpdateStep() {
        return stepBuilderFactory.get("transactionUpdateStep")
                .<AccountVo, Boolean>chunk(10) // 청크 단위로 데이터 처리
                .reader(accountItemReader())
                .processor(accountItemProcessor())
                .writer(accountItemWriter())
                .faultTolerant()
                .retry(Exception.class) // 에러 발생시 재시도
                .retryLimit(2)          // 최대 2회 재시도
                .skip(Exception.class)  // 재시도 후 실패 시 건너뜀
                .build();
    }

    @Bean
    public ItemReader<AccountVo> accountItemReader() {
        return new ItemReader<AccountVo>() {
            private int nextAccountIndex = 0;
            private List<AccountVo> accounts = accountMapper.findAllAccounts();
            private Set<String> processedAccounts = new HashSet<>(); // 처리된 계좌 목록

            @Override
            public AccountVo read() {
                log.info("reader 호출");
                while (nextAccountIndex < accounts.size()) {
                    AccountVo account = accounts.get(nextAccountIndex++);
                    if (!processedAccounts.contains(account.getAccountNum())) {
                        processedAccounts.add(account.getAccountNum()); // 처리된 계좌 저장
                        return account;
                    }
                }
                return null; // 모든 계좌 처리 완료
            }
        };
    }

    @Bean
    public ItemProcessor<AccountVo, Boolean> accountItemProcessor() {
        return new ItemProcessor<AccountVo, Boolean>() {
            @Override
            public Boolean process(AccountVo account) throws Exception {
                Long userId = account.getUserId();
                String accountNum = account.getAccountNum();
                try {
                    // 거래 내역 저장
                    homeService.saveTransactions(userId, accountNum, 1);
                    return true; // 처리 성공
                } catch (Exception e) {
                    return false; // 처리 실패
                }
            }
        };
    }

    @Bean
    public ItemWriter<Boolean> accountItemWriter() {
        return new ItemWriter<Boolean>() {
            @Override
            public void write(List<? extends Boolean> items) throws Exception {
                // 처리 결과를 로그로 남기거나 추가 작업을 수행
                for (Boolean success : items) {
                    if (success) {
                        // 성공적인 처리에 대한 로깅
                        log.info("거래내역 업데이트 성공 - batch");
                    } else {
                        // 실패에 대한 로깅
                        log.info("거래 내역 업데이트 실패 - batch");
                    }
                }
            }
        };
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

    @Bean(name = "updateMonthlyMissionJob")
    public Job updateMonthlyMissionJob() {
        return jobBuilderFactory.get("updateMonthlyMissionJob")
                .start(updateMonthlyMissionStep())
                .build();
    }

    @Bean
    public Step updateMonthlyMissionStep() {
        return stepBuilderFactory.get("updateMonthlyMissionStep")
                .tasklet((contribution, chunkContext) -> {
                    missionService.updateMonthlyMissionForAllUsers();
                    return null;
                }).build();
    }

    @Bean(name = "dailyCheckMonthlyMissionsJob")
    public Job dailyCheckMonthlyMissionsJob() {
        return jobBuilderFactory.get("dailyCheckMonthlyMissionsJob")
                .start(dailyCheckMonthlyMissionsStep())
                .build();
    }

    @Bean
    public Step dailyCheckMonthlyMissionsStep() {
        return stepBuilderFactory.get("dailyCheckMonthlyMissionsStep")
                .tasklet((contribution, chunkContext) -> {
                    missionService.dailyCheckMonthlyMissions();
                    return null;
                }).build();
    }

    @Bean(name = "checkEndOfMonthMissionsJob")
    public Job checkEndOfMonthMissionsJob() {
        return jobBuilderFactory.get("checkEndOfMonthMissionsJob")
                .start(checkEndOfMonthMissionsStep())
                .build();
    }

    @Bean
    public Step checkEndOfMonthMissionsStep() {
        return stepBuilderFactory.get("checkEndOfMonthMissionsStep")
                .tasklet((contribution, chunkContext) -> {
                    missionService.checkEndOfMonthMissions();
                    return null;
                }).build();
    }

}

