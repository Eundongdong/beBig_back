package beBig.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DailyMissionResponseDto {
    private int personalDailyMissionId;
    private String missionTopic;
    private int missionId;
    private boolean personalDailyMissionCompleted;
    private int missionType;
}
