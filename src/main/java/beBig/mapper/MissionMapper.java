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

    void completeMonthlyMission(@Param("personalMissionId") long personalMissionId);

    void completeDailyMission(@Param("personalMissionId") long personalMissionId);

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

    // 신규 월간 미션 삽입
    void insertMonthlyMission(@Param("userId") Long userId, @Param("missionId") int missionId);

    // 현재 월간 미션 ID 가져오기
    int getCurrentMonthlyMissionId(Long userId);

    // 월간 미션 완료
    void completeMonthlyMission(@Param("personalMissionId") long personalMissionId,@Param("personalMonthlyMissionCompleted") int personalMonthlyMissionCompleted);

    // 월간 미션 업데이트
    void updateMonthlyMission(@Param("userId") Long userId, @Param("missionId") int missionId);

    // 현재 월간 미션 정보 가져오기
    PersonalMonthlyMissionVo getCurrentMonthlyMission(@Param("userId")Long userId);

    // 커뮤니티 글 수 카운트
    int countCommunityPosts(@Param("userId")Long userId);

    // 좋아요 수 카운트
    int countPostLikesInMonth(@Param("userId")Long userId);

    // 소비 차이 계산
    int calculateSpendingDifference(@Param("userId")Long userId);

    // 저축 차이 계산
    int calculateSavingDifference(@Param("userId")Long userId);

    // 데일리 미션 완료 수 카운트
    int countCompletedDailyMissions(@Param("userId")Long userId);

    // 모든 사용자 ID 가져오기
    List<Long> findAllUsersWithMonthlyMissions();

//    // 저축 계좌 수 카운트
//    int countSavingsAccounts(@Param("userId")Long userId);

    void updateMonthlyMissionStatus(@Param("personalMonthlyMissionId")int personalMonthlyMissionId, @Param("status") int status);

    //오류방지
    int countUserPosts(@Param("thisYear") int thisYear, @Param("thisMonth") int thisMonth, @Param("userId") long userId);

    int countUserLikes(@Param("thisYear") int thisYear, @Param("thisMonth") int thisMonth, @Param("userId") long userId);

    int getMonthlyConsumption(@Param("thisYear") int thisYear, @Param("thisMonth") int thisMonth, @Param("accountNum") String accountNum);

    List<String> getAccountListByUserId(@Param("userId") long userId);

    long findMonthlyMissionIdByUserId(@Param("userId") long userId);

    int getDailyConsumption(@Param("thisYear") int thisYear, @Param("thisMonth") int thisMonth, @Param("day") int day, @Param("accountNum") String accountNum);

}

