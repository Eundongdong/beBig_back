package beBig.vo;

public class PersonalMonthlyMissionVo {
    private int personalMonthlyMissionNo;
    private int personalMonthlyMissionId;
    private Long personalMonthlyMissionUserId;
    private boolean personalMonthlyMissionCompleted;

    private MissionVo mission; // FK - 월간 미션
}
