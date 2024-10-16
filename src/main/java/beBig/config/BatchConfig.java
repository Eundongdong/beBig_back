package beBig.config;

import beBig.mapper.AccountMapper;
import beBig.mapper.UserMapper;
import beBig.service.HomeService;
import beBig.service.MissionService;
import beBig.service.UserService;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfigurer {
    private final UserMapper userMapper;
    private final HomeService homeService;
    private final UserService userService;
    private final MissionService missionService;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final AccountMapper accountMapper;

    public BatchConfig(UserMapper userMapper, JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, HomeService homeService, UserService userService, MissionService missionService, AccountMapper accountMapper) {
        this.userMapper = userMapper;
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
                .<AccountVo, Boolean>chunk(20) // 청크 단위로 데이터 처리
                .reader(accountItemReader())
                .processor(accountItemProcessor())
                .writer(accountItemWriter())
                .faultTolerant()
                .retry(Exception.class) // 에러 발생시 재시도
                .retryLimit(2)          // 최대 2회 재시도
                .skip(Exception.class)  // 재시도 후 실패 시 건너뜀
                .taskExecutor(taskExecutor()) // 비동기 처리
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
                log.info("청크 쓰기 시작 - 처리된 항목 수: {}", items.size()); // 청크 쓰기 시작 로그
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
                .incrementer(new RunIdIncrementer())
                .flow(ageUpdateStep())
                .end()
                .build();
    }

    @Bean
    public Step ageUpdateStep() {
        return stepBuilderFactory.get("ageUpdateStep")
                .<UserVo, UserVo>chunk(100) // 청크 단위 설정
                .reader(userItemReader())
                .processor(userItemProcessor())
                .writer(userItemWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(2)
                .taskExecutor(taskExecutor()) // 멀티스레드 실행
                .build();
    }

    @Bean
    public ItemReader<UserVo> userItemReader() {
        return new ItemReader<UserVo>() {
            private List<UserVo> users = userMapper.getAllUsers(); // 사용자 목록 조회
            private int currentIndex = 0;

            @Override
            public UserVo read() throws Exception {
                if (currentIndex < users.size()) {
                    return users.get(currentIndex++);
                }
                return null; // 모든 사용자 처리 완료
            }
        };
    }

    @Bean
    public ItemProcessor<UserVo, UserVo> userItemProcessor() {
        return user -> {
            if (user.getUserBirth() == null) return null; // 생일 없는 경우 제외

            LocalDate today = LocalDate.now();
            int age = today.getYear() - user.getUserBirth().toLocalDate().getYear();
            user.setUserAgeRange((age / 10) * 10); // 나이 범위 설정
            return user;
        };
    }

    @Bean
    public ItemWriter<UserVo> userItemWriter() {
        return users -> {
            userMapper.updateUsersAgeRanges((List<UserVo>) users); // 일괄 업데이트 수행
            log.info("청크 단위 나이 범위 업데이트 완료 - {}명", users.size());
        };
    }

    // TaskExecutor 설정
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // 최소 스레드 수
        executor.setMaxPoolSize(4); // 최대 스레드 수
        executor.setQueueCapacity(24); // 큐 용량
        executor.setThreadNamePrefix("age-update-thread-");
        executor.initialize();
        return executor;
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

