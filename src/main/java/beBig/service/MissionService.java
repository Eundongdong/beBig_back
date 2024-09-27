package beBig.service;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;

import java.util.List;

public interface MissionService {
    MonthlyMissionResponseDto showMonthlyMission(long userId);
    List<DailyMissionResponseDto> showDailyMission(long userId);
    void completeMonthlyMission(long personalMissionId);
    void completeDailyMission(long personalMissionId);
}
