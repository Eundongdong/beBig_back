package beBig.mapper;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MissionMapper {
    List<DailyMissionResponseDto> getPersonalDailyMission(@Param("userId") long userId);

    MonthlyMissionResponseDto getPersonalMonthlyMission(@Param("userId") long userId);

    void completeMonthlyMission(@Param("personalMissionId") long personalMissionId);

    void completeDailyMonthlyMission(@Param("personalMissionId") long personalMissionId);

    int findSalaryByUserId(@Param("userId") long userId);

    double findSaveRateByUserIdAndMissionId(@Param("userId") long userId, @Param("missionId") long missionId);

    double findUseRateByUserIdAndMissionId(@Param("userId") long userId, @Param("missionId") long missionId);

    int findMissionCategoryByMissionId(@Param("missionId") long missionId);

    //mission score 갱신 듀오 가져오고 업데이트
    int findCurrentMissionMonthScoreByUserId(@Param("userId") long userId);

    void updateCurrentMissionMonthScoreByUserId(@Param("userId") long userId, @Param("score") int score);

    long findMissionIsCompletedByPersonalMissionId(@Param("personalMissionId") long personalMissionId);

    // 모든 사용자 personal_daily_mission_id 테이블에 존재하는 userId 조회
    List<Long> findAllUsersWithDailyMissions();

    // type에 따른 랜덤 미션 조회
    List<Integer> findRandomMissionsByType(@Param("missionType") int missionType, @Param("count") int count);

    // 기존 일일 미션 조회
    List<Integer> findExistingDailyMissions(@Param("userId") Long userId);

    // 사용자의 자산 상태 체크
    int countUserAssetStatus(@Param("userId") Long userId);

    // 기존 사용자의 일일 미션 업데이트
    void updateDailyMission(@Param("userId") Long userId,
                            @Param("personalDailyMissionId") int personalDailyMissionId,
                            @Param("missionId") Integer missionId);

    // 신규 미션 삽입
    void insertDailyMission(@Param("userId") Long userId, @Param("missionId") int missionId);

    int findExistingDailyMissionsCount(@Param("userId") Long userId);
}

