package beBig.service;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.mapper.MissionMapper;
import beBig.mapper.UserMapper;
import beBig.vo.MissionVo;
import beBig.vo.PersonalMonthlyMissionVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MissionServiceImp implements MissionService {
    private final SqlSessionTemplate sqlSessionTemplate;
    private final UserMapper userMapper;
    private final MissionMapper missionMapper;

    @Autowired
    public MissionServiceImp(SqlSessionTemplate sqlSessionTemplate, UserMapper userMapper, MissionMapper missionMapper) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.userMapper = userMapper;
        this.missionMapper = missionMapper;
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

    // 미션 비율 조회
    @Override
    public double findRate(long userId, long missionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        int missionType = missionMapper.findMissionCategoryByMissionId(missionId);
        if (missionType == 2) {
            return missionMapper.findSaveRateByUserIdAndMissionId(userId, missionId);
        } else if (missionType == 3) {
            return missionMapper.findUseRateByUserIdAndMissionId(userId, missionId);
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
    public int updateScore(long userId, int amount) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        int currentScore = missionMapper.findCurrentMissionMonthScoreByUserId(userId) + amount;
        missionMapper.updateCurrentMissionMonthScoreByUserId(userId, currentScore);
        return currentScore;
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
        }
    }

    // 매일 체크하는 월간 미션 수행 확인 - 일간 batch
    public void dailyCheckMonthlyMissions() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            PersonalMonthlyMissionVo currentMission = missionMapper.getCurrentMonthlyMission(userId);

            switch (currentMission.getMissionId()) {
                case 1:
//                    if (missionMapper.countSavingsAccounts(userId) > 0) {
//                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
//                    } else {
//                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
//                    }
//                    break;
                    log.info("case 1 undefined yet...");
                    break;
                case 2:
                    if (missionMapper.countCommunityPosts(userId) >= 4) {
                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                case 3:
                    if (missionMapper.countPostLikesInMonth(userId) >= 50) {
                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                case 6:
                    if (missionMapper.countCompletedDailyMissions(userId) > 0) {
                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                default:
                    log.warn("Unknown daily mission ID: " + currentMission.getMissionId());
            }
        }
    }

    // n 값을 결정하는 메서드 -> ??????????
    public int determineNValue(Long userId) {
        return 0;
    }

    // 월말 체크하는 월간 미션 수행 확인  - 월말 batch
    public void checkEndOfMonthMissions() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            int n = determineNValue(userId);
            PersonalMonthlyMissionVo currentMission = missionMapper.getCurrentMonthlyMission(userId);

            switch (currentMission.getMissionId()) {
//                case 4:
//                    if (missionMapper.calculateSpendingDifference(userId) < n) {
//                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
//                    } else {
//                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
//                    }
//                    break;
//                case 5:
//                    int savingDifference = missionMapper.calculateSavingDifference(userId);
//                    if (savingDifference >= n) {
//                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
//                    } else {
//                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
//                    }
//                    break;

                default:
                    log.warn("Unknown end of month mission ID: " + currentMission.getMissionId());
            }
        }
    }


    // 상태 업데이트 메서드들
    public void markMissionAsInProgress(int personalMissionId) {
        updateMissionStatus(personalMissionId, 0); // 진행 중 상태
    }

    public void markMissionAsComplete(int personalMissionId) {
        updateMissionStatus(personalMissionId, 1); // 완료 상태
    }

    public void markMissionAsFailed(int personalMissionId) {
        updateMissionStatus(personalMissionId, 2); // 실패 상태
    }

    private void updateMissionStatus(int personalMissionId, int status) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.updateMonthlyMissionStatus(personalMissionId, status);
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
        return missionMapper.findMonthlyMissionIdByUserId(userId);
    }

    @Override
    public boolean hasMonthlyMissionSucceeded(long missionId, long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        int thisMonth = LocalDate.now().getMonthValue();

        if (missionId == 1) {

        }
        if (missionId == 2) {
            return missionMapper.countUserPosts(thisMonth, userId) >= 4;
        }
        if (missionId == 3) {
            return missionMapper.countUserLikes(thisMonth, userId) >= 50;
        }
        if (missionId == 4) {
            //개인별 n원 가져오기
            int target = countNValue(missionId, userId); // = n원
            int totalConsumption = 0;
            List<String> accountList = missionMapper.getAccountListByUserId(userId);
            for (String accountNum : accountList) {
                int consumption = missionMapper.getMonthlyConsumption(thisMonth, accountNum);
                totalConsumption += consumption;
            }
            return totalConsumption >= target;
        }
        if (missionId == 5) {
            // 한 달 동안 매일 n원씩 저축 미션
            int N = countNValue(missionId, userId);

            List<String> accountList = missionMapper.getAccountListByUserId(userId);

            // 지난달의 총 지출 가져오기
            int lastMonth = thisMonth == 1 ? 12 : thisMonth - 1; // 지난달 계산
            int lastYear = thisMonth == 1 ? LocalDate.now().getYear() - 1 : LocalDate.now().getYear();
            int lastMonthTotalConsumption = 0;

            for (String accountNum : accountList) {
                int consumption = missionMapper.getMonthlyConsumption(lastMonth, accountNum);
                lastMonthTotalConsumption += consumption;
            }

            // 지난달 일수 계산 (윤년을 고려한 일수 계산)
            YearMonth yearMonthObject = YearMonth.of(lastYear, lastMonth);
            int lastDay = yearMonthObject.lengthOfMonth(); // 지난달의 일 수
            int lastConsumptionPerDay = lastMonthTotalConsumption / lastDay;// 지난달 일별 소비 금액 계산
            int target = lastConsumptionPerDay - N; // 목표 소비금액. 해당 값 이하일시 성공으로 처리

            // 오늘까지 매일의 소비가 목표 금액 이하인지 확인
            int todayDate = LocalDate.now().getDayOfMonth(); // 현재 월의 오늘 날짜
            for (int i = 1; i <= todayDate; i++) {
                for (String accountNum : accountList) {
                    int consumption = missionMapper.getDailyConsumption(thisMonth, i, accountNum);
                    if (consumption > target) return false;
                }
            }
            return true;
        }
        if (missionId == 6) {

        }

        return false;
    }

    public int countNValue(long missionId, long userId) {
        int salary = findSalary(userId);
        double rate = findRate(userId, missionId);
        return (int) (salary * (rate / 100));
    }
}
