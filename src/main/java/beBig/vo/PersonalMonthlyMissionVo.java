package beBig.vo;

import lombok.Data;

@Data
public class PersonalMonthlyMissionVo {
    private int personalMonthlyMissionId;
    private int missionId;
    private Long userId;
    private int personalMonthlyMissionCompleted;
    private int previousSavingsAccountCount;
}
