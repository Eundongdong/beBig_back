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
    // 특정 userId에 해당하는 Daily Mission 목록을 가져옴
    List<DailyMissionResponseDto> getPersonalDailyMission(@Param("userId") long userId);

    // 특정 userId에 해당하는 가장 최근의 Monthly Mission을 가져옴
    MonthlyMissionResponseDto getPersonalMonthlyMission(@Param("userId") long userId);

    void completeMonthlyMission(@Param("personalMissionId") long personalMissionId);
    void completeDailyMonthlyMission(@Param("personalMissionId") long personalMissionId);
}

