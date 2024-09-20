package beBig.vo;

public class PersonalDailyMissionVo {
    private int personalDailyMissionNo;
    private int personalDailyMissionId;
    private Long personalDailyMissionUserId;
    private boolean personalDailyMissionCompleted;

    private MissionVo mission; // FK - 일일 미션
}
