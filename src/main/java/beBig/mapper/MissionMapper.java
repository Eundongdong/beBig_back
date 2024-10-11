package beBig.mapper;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.vo.PersonalMonthlyMissionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MissionMapper {
    List<DailyMissionResponseDto> getPersonalDailyMission(@Param("userId") long userId);

    MonthlyMissionResponseDto getPersonalMonthlyMission(@Param("userId") long userId);

    void completeDailyMission(@Param("personalMissionId") long personalMissionId);

    int findSalaryByUserId(@Param("userId") long userId);

    // 수정필요구간
    double findSaveRateByUserId(@Param("userId") long userId);

    double findUseRateByUserId(@Param("userId") long userId);
    // 수정필요구간

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

    // 신규 월간 미션 삽입
    void insertMonthlyMission(@Param("userId") Long userId, @Param("missionId") int missionId);

    // 현재 월간 미션 ID 가져오기
    int getCurrentMonthlyMissionId(Long userId);

    // 월간 미션 완료
    void completeMonthlyMission(@Param("personalMissionId") long personalMissionId, @Param("personalMonthlyMissionCompleted") int personalMonthlyMissionCompleted);

    // 월간 미션 업데이트
    void updateMonthlyMission(@Param("userId") Long userId, @Param("missionId") int missionId);

    // 현재 월간 미션 정보 가져오기
    PersonalMonthlyMissionVo getCurrentMonthlyMission(@Param("userId") Long userId);

    // 데일리 미션 완료 수 카운트
    int countCompletedDailyMissions(@Param("userId") Long userId);

    // 모든 사용자 ID 가져오기
    List<Long> findAllUsersWithMonthlyMissions();

    void updateMonthlyMissionStatus(@Param("personalMonthlyMissionId") int personalMonthlyMissionId, @Param("status") int status);

    //오류방지
    int countUserPosts(@Param("thisYear") int thisYear, @Param("thisMonth") int thisMonth, @Param("userId") long userId);

    int countUserLikes(@Param("thisYear") int thisYear, @Param("thisMonth") int thisMonth, @Param("userId") long userId);

    Integer getMonthlyConsumption(@Param("thisYear") int thisYear, @Param("thisMonth") int thisMonth, @Param("accountNum") String accountNum);

    List<String> getAccountListByUserId(@Param("userId") long userId);

    long findMonthlyMissionIdByUserId(@Param("userId") long userId);


    int findPersonalMissionIdByUserId(@Param("userId") long userId);

    Integer findBalanceOnFirstDay(@Param("thisYear") int thisYear,
                         @Param("thisMonth") int thisMonth,
                         @Param("day") int day,
                         @Param("accountNum") String accountNum);

    Integer findBalanceOnLastDay(@Param("thisYear") int thisYear,
                              @Param("thisMonth") int thisMonth,
                              @Param("day") int day,
                              @Param("accountNum") String accountNum);


    // personal_monthly_mission 테이블에 previous_savings_account_count 저장하는 메서드 (월초에 호출)
    void updatePreviousSavingsAccountCount(@Param("userId") long userId, @Param("count") int count);

    // personal_monthly_mission 테이블에서 previous_savings_account_count 값을 가져오는 메서드
    Integer getPreviousSavingsAccountCount(@Param("userId") long userId);

    // account 테이블에서 account_type이 12인 계좌의 갯수를 가져오는 메서드 (현재 갯수 확인용)
    Integer countCurrentSavingsAccounts(@Param("userId") long userId);

    // 월업데이트 - 현재 -> 지난달 / 현재 = 0
    void updateLastMonthScore(@Param("userId") long userId);

    // 소비 차이 계산0
    int calculateSpendingDifference(@Param("userId") Long userId, @Param("accountNum") String accountNum);

    // 저축 차이 계산
    int calculateSavingDifference(@Param("userId") Long userId, @Param("accountNum") String accountNum);

}

