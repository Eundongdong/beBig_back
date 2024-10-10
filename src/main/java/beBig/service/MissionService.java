package beBig.service;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;

import java.util.List;

public interface MissionService {
    MonthlyMissionResponseDto showMonthlyMission(long userId);

    List<DailyMissionResponseDto> showDailyMission(long userId);

    void completeMonthlyMission(long personalMissionId);

    void completeDailyMission(long personalMissionId);

    double findRate(long userId, long missionId);

    int findSalary(long userId);

    //점수계산
    int updateScore(long userId, int amount);

    long findIsCompleted(long personalMissionId);

    int findCurrentMonthScore(long userId);

    void assignDailyMission();

    void updateDailyMissionForUser(Long userId);

    void addDailyMissions(Long userId);

    // 월간 미션 업데이트 - 월초 batch
    void updateMonthlyMissionForAllUsers();

    void dailyCheckMonthlyMissions();

    void checkEndOfMonthMissions();
}
