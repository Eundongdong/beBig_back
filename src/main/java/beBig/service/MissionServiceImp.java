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

    //월간미션
    @Override
    public MonthlyMissionResponseDto showMonthlyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        MonthlyMissionResponseDto monthlyMissionResponseDto = missionMapper.getPersonalMonthlyMission(userId);
        return monthlyMissionResponseDto;
    }

    @Override
    public List<DailyMissionResponseDto> showDailyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);

        return missionMapper.getPersonalDailyMission(userId);
    }

    @Override
    public void completeMonthlyMission(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeMonthlyMission(personalMissionId);
    }

    @Override
    public void completeDailyMission(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeDailyMonthlyMission(personalMissionId);
    }

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

    @Override
    public int findSalary(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findSalaryByUserId(userId);
    }

    //점수계산
    @Override
    public int updateScore(long userId, int amount) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        //월별점수가져옴
        int currentScore = missionMapper.findCurrentMissionMonthScoreByUserId(userId) + amount;
        missionMapper.updateCurrentMissionMonthScoreByUserId(userId, currentScore);
        return currentScore;
    }

    @Override
    public long findIsCompleted(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findMissionIsCompletedByPersonalMissionId(personalMissionId);
    }

    @Override
    public int findCurrentMonthScore(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findCurrentMissionMonthScoreByUserId(userId);
    }

    // 전체 사용자 업데이트
    public void assignDailyMission() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);  // Mapper를 동적으로 가져옴
        List<Long> userIds = missionMapper.findAllUsersWithDailyMissions(); // 모든 사용자 ID 조회

        for (Long userId : userIds) {
            updateDailyMissionForUser(userId); // 미션 갱신
        }
    }

    // 사용자에 대한 미션 갱신
    public void updateDailyMissionForUser(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);  // Mapper를 동적으로 가져옴
        // type이 2인 미션을 랜덤으로 3개 가져옴
        List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);

        // 기존 personal_daily_mission_id를 가져옴
        List<Integer> existingMissionIds = missionMapper.findExistingDailyMissions(userId);

        // 기존 미션이 있을 경우 mission_id, 완료여부 0으로 업데이트
        if (!existingMissionIds.isEmpty()) {
            for (int i = 0; i < Math.min(dailyMissions.size(), existingMissionIds.size()); i++) {
                missionMapper.updateDailyMission(userId, existingMissionIds.get(i), dailyMissions.get(i)); // 각 미션 ID를 업데이트
            }
        }
    }

    // 신규 미션 추가
    public void addDailyMissions(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);  // Mapper를 동적으로 가져옴
        // 자산 상태 체크
        boolean isAssetAndSurveyLoaded = missionMapper.countUserAssetStatus(userId) > 0;

        // 자산과 설문조사가 모두 완료된 경우
        if (isAssetAndSurveyLoaded) {
            // 기존 미션이 있는지 확인하기 위해 count 쿼리 사용
            int existingMissionCount = missionMapper.findExistingDailyMissionsCount(userId); // 새로운 메서드 추가

            // 기존 미션이 없을 경우
            if (existingMissionCount == 0) {
                List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);
                // 신규 미션 삽입
                for (int dailyMission : dailyMissions) {
                    missionMapper.insertDailyMission(userId, dailyMission);
                }
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
        return missionMapper.findMonthlyMissionIdByUserId(userId);
    }

    @Override
    public boolean hasMonthlyMissionSucceeded(long missionId, long userId) {
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
