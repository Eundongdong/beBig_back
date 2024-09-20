package beBig.vo;

import lombok.Data;

@Data
public class MissionVo {
    private int missionId;
    private int missionType; // 월간, 일간
    private String missionTopic;
    private int missionCategory; // 행동, 지출, 소비
}
