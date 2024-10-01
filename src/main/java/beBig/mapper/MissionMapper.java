package beBig.mapper;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.vo.MissionVo;
import beBig.vo.PersonalDailyMissionVo;
import beBig.vo.PersonalMonthlyMissionVo;
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
}

