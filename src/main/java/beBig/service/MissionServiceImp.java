package beBig.service;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.mapper.MissionMapper;
import beBig.vo.PersonalMonthlyMissionVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
public class MissionServiceImp implements MissionService {
    private final SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    public MissionServiceImp(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    // 월간 미션 조회
    @Override
    public MonthlyMissionResponseDto showMonthlyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.getPersonalMonthlyMission(userId);
    }

    // 일간 미션 조회
    @Override
    public List<DailyMissionResponseDto> showDailyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.getPersonalDailyMission(userId);
    }

    // 월간 미션 완료 처리
    @Override
    public void completeMonthlyMission(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeMonthlyMission(personalMissionId, 1);
    }

    // 일간 미션 완료 처리
    @Override
    public void completeDailyMission(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeDailyMission(personalMissionId);
    }

    @Override
    public double findRate(long userId, long missionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        int missionType = missionMapper.findMissionCategoryByMissionId(missionId);
        if (missionType == 2) { // 저축 - save rate 찾기
            return missionMapper.findSaveRateByUserId(userId);
        } else if (missionType == 3) { // 사용 - use rate 찾기
            return missionMapper.findUseRateByUserId(userId);
        }
        return 0.0;
    }

    // 사용자 급여 조회
    @Override
    public int findSalary(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findSalaryByUserId(userId);
    }

    // 점수 계산 및 업데이트
    @Override
    public void updateScore(long userId, int amount) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        //월별점수가져옴
        int currentScore = missionMapper.findCurrentMissionMonthScoreByUserId(userId);
        // 새로운 점수를 계산
        int newScore = currentScore + amount;
        // 계산된 점수를 업데이트
        missionMapper.updateCurrentMissionMonthScoreByUserId(userId, newScore);
    }

    // 미션 완료 여부 조회
    @Override
    public long findIsCompleted(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findMissionIsCompletedByPersonalMissionId(personalMissionId);
    }

    // 현재 월 점수 조회
    @Override
    public int findCurrentMonthScore(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findCurrentMissionMonthScoreByUserId(userId);
    }

    // 전체 사용자 일간 미션 갱신
    public void assignDailyMission() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithDailyMissions();

        for (Long userId : userIds) {
            updateDailyMissionForUser(userId);
        }
    }

    // 사용자 미션 갱신
    public void updateDailyMissionForUser(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);
        List<Integer> existingMissionIds = missionMapper.findExistingDailyMissions(userId);

        if (!existingMissionIds.isEmpty()) {
            for (int i = 0; i < Math.min(dailyMissions.size(), existingMissionIds.size()); i++) {
                missionMapper.updateDailyMission(userId, existingMissionIds.get(i), dailyMissions.get(i));
            }
        }
    }

    // 신규 일간 미션 추가
    public void addDailyMissions(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        boolean isAssetAndSurveyLoaded = missionMapper.countUserAssetStatus(userId) > 0;

        if (isAssetAndSurveyLoaded) {
            int existingMissionCount = missionMapper.findExistingDailyMissionsCount(userId);

            if (existingMissionCount == 0) {
                List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);
                for (int dailyMission : dailyMissions) {
                    missionMapper.insertDailyMission(userId, dailyMission);
                }
                generateMonthlyMission(userId);
            }
        }
    }

    // 신규 월간 미션 생성
    public void generateMonthlyMission(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.insertMonthlyMission(userId, 1);
        log.info("1번 미션 할당");
        // 1. account 테이블에서 현재 account_type이 12인 계좌의 갯수를 가져오기
        int currentSavingsAccountCount = missionMapper.countCurrentSavingsAccounts(userId);
        // 2. personal_monthly_mission 테이블에 해당 갯수를 저장하기
        missionMapper.updatePreviousSavingsAccountCount(userId, currentSavingsAccountCount);
    }

    // 월간 미션 업데이트 - 월초 batch
    @Override
    public void updateMonthlyMissionForAllUsers() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            int currentMissionId = missionMapper.getCurrentMonthlyMissionId(userId);
            int nextMissionId = (currentMissionId % 6) + 1;  // 미션 순환
            missionMapper.updateMonthlyMission(userId, nextMissionId);
            log.info(nextMissionId + "번 미션으로 업데이트");
            missionMapper.updateLastMonthScore(userId);
            if (nextMissionId == 1) {
                // 1. account 테이블에서 현재 account_type이 12인 계좌의 갯수를 가져오기
                int currentSavingsAccountCount = missionMapper.countCurrentSavingsAccounts(userId);

                // 2. personal_monthly_mission 테이블에 해당 갯수를 저장하기
                missionMapper.updatePreviousSavingsAccountCount(userId, currentSavingsAccountCount);
            }
        }

    }

    // 매일 체크하는 월간 미션 수행 확인 - 일간 batch
    public void dailyCheckMonthlyMissions() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            PersonalMonthlyMissionVo currentMission = missionMapper.getCurrentMonthlyMission(userId);
            int missionId = currentMission.getMissionId();
            if (currentMission.getPersonalMonthlyMissionCompleted() == -1) continue;
            boolean isSucceed;
            switch (missionId) {
                case 1:
                case 2:
                case 3:
                    isSucceed = hasMonthlyMissionSucceeded(missionId, userId);
                    if (isSucceed) {
                        succeedMonthlyMission(userId);
                    } else {
                        //실패로직 - FALSE일시 값을 진행중-아직 달성 못함 으로 바꿔야 함 -> 0
                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
                        // -1 이면 실패 0 이면 진행중 1이면 성공
                    }
                    break;
                case 6:
                    isSucceed = hasMonthlyMissionSucceeded(missionId, userId);
                    if (isSucceed) {
                        succeedMonthlyMission(userId);
                    } else {
                        //실패로직 - FALSE일시 값을 실패으로 바꿔야 함 -> -1
                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                default:
                    log.warn("Unknown daily mission ID: " + currentMission.getMissionId());
            }
        }
    }

    // 월말 체크하는 월간 미션 수행 확인  - 월말 batch
    public void checkEndOfMonthMissions() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            PersonalMonthlyMissionVo currentMission = missionMapper.getCurrentMonthlyMission(userId);
            int missionId = currentMission.getMissionId();

            switch (missionId) {
                case 4:
                case 5:
                    boolean isSucceed = hasMonthlyMissionSucceeded(missionId, userId);
                    if (isSucceed) {
                        succeedMonthlyMission(userId);
                    } else {
                        //실패로직 - FALSE일시 값을 실패으로 바꿔야 함 -> -1
                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;

                default:
                    log.warn("Unknown end of month mission ID: " + currentMission.getMissionId());
            }
        }
    }

    @Override
    // 오늘로부터 이번 달이 끝날 때까지 남은 일수를 반환하는 메서드
    public int getRestDaysInCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        return (int) ChronoUnit.DAYS.between(today, lastDayOfMonth);
    }

    @Override
    // 이번 달의 총 일수를 반환하는 메서드
    public int getDaysInCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        return lastDayOfMonth.getDayOfMonth() - firstDayOfMonth.getDayOfMonth() + 1;
    }

    @Override
    // N 존재하는지 판별하고 'n'을 숫자로 변환
    public String replaceNWithNumber(String s, int number, double rate) {
        StringBuilder result = new StringBuilder();
        int calSalary = (int) (number * (rate / 100));

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'n') {
                result.append(calSalary);
            } else {
                result.append(s.charAt(i));
            }
        }
        return result.toString();
    }

    @Override
    public long getMonthlyMissionNumber(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findMonthlyMissionIdByUserId(userId);
    }

    @Override
    public boolean hasMonthlyMissionSucceeded(long missionId, long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        int thisMonth = LocalDate.now().getMonthValue();
        int thisYear = LocalDate.now().getYear();
        int today = LocalDate.now().getDayOfMonth();

        // 이번 달의 마지막 날짜 (말일) 계산
        YearMonth currentMonth = YearMonth.of(thisYear, thisMonth);
        int lastDayOfMonth = currentMonth.lengthOfMonth(); // 말일 계산

        if (missionId == 1) {
            // 1번 미션: 예/적금 계좌가 추가되었는지 확인

            // 1. personal_monthly_mission에서 이전에 저장된 예/적금 계좌 수 가져오기
            int previousCount = missionMapper.getPreviousSavingsAccountCount(userId);

            // 2. account 테이블에서 현재 account_type이 12인 계좌의 갯수를 가져오기
            int currentCount = missionMapper.countCurrentSavingsAccounts(userId);

            // 3. 현재 계좌 수가 이전보다 증가했으면 미션 성공
            return currentCount > previousCount;
        }

        if (missionId == 2) {
            // 현재 연도와 월에 대해 사용자가 4개 이상의 게시글을 작성했는지 확인
            return missionMapper.countUserPosts(thisYear, thisMonth, userId) >= 4;
        }

        if (missionId == 3) {
            // 현재 연도와 월에 대해 사용자의 좋아요 합이 50 이상인지 확인
            return missionMapper.countUserLikes(thisYear, thisMonth, userId) >= 50;
        }

        if (missionId == 4) {
            // 개인별 N원 목표 설정 후 소비 확인
            int target = countNValue(missionId, userId); // 목표 금액
            List<String> accountNums = missionMapper.getAccountListByUserId(userId); // 모든 계좌 번호 리스트

            int totalConsumption = 0;

            for (String accountNum : accountNums) {
                // 각 계좌에 대한 소비 금액 계산
                Integer consumption = missionMapper.calculateSpendingDifference(userId, accountNum);
                if (consumption != null) {
                    totalConsumption += consumption;
                }
            }

            // 목표 금액 이하 소비했는지 확인
            return totalConsumption >= target;
        }

        if (missionId == 5) {
            // 한달동안 N원을 모아보세요
            int target = countNValue(missionId, userId); // 일정금액
            List<String> accountNums = missionMapper.getAccountListByUserId(userId); // 모든 계좌 번호 리스트

            int totalSavingDifference = 0;

            for (String accountNum : accountNums) {
                // 각 계좌에 대한 저축 금액 계산
                Integer savingDifference = missionMapper.calculateSavingDifference(userId, accountNum);
                if (savingDifference != null) {
                    totalSavingDifference += savingDifference;
                }
            }

            // 목표 금액 이상 저축했는지 확인
            return totalSavingDifference >= target;
        }
        if (missionId == 6) {
            return missionMapper.countCompletedDailyMissions(userId) > 0;
        }

        return false;
    }


    //N값 계산기
    public int countNValue(long missionId, long userId) {
        int salary = findSalary(userId);
        double rate = findRate(userId, missionId);
        return (int) (salary * (rate / 100));
    }

    // 상태 업데이트 메서드들
    public void markMissionAsInProgress(int personalMissionId) {
        updateMissionStatus(personalMissionId, 0); // 진행 중 상태
    }

    public void markMissionAsComplete(int personalMissionId) {
        updateMissionStatus(personalMissionId, 1); // 완료 상태
    }

    public void markMissionAsFailed(int personalMissionId) {
        updateMissionStatus(personalMissionId, -1); // 실패 상태
    }

    private void updateMissionStatus(int personalMissionId, int status) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.updateMonthlyMissionStatus(personalMissionId, status);
    }

    public void succeedMonthlyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        int totalDays = getDaysInCurrentMonth();
        updateScore(userId, 100 - (totalDays * 2));
        log.info("userId = " + userId + ", totalDays = " + totalDays);
        Integer monthlyMissionId = missionMapper.findPersonalMissionIdByUserId(userId);
        log.info("Monthly mission id: " + monthlyMissionId);
        if (monthlyMissionId == null) {
            throw new IllegalArgumentException("해당 사용자에 대한 월간 미션 ID가 존재하지 않습니다.");
        }

        completeMonthlyMission(monthlyMissionId);
    }
}
