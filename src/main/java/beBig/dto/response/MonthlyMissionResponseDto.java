package beBig.dto.response;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MonthlyMissionResponseDto {
    private int personalMonthlyMissionId;
    private String missionTopic;
    private int missionId;
    private boolean personalMonthlyMissionCompleted;
    private int missionType;
}
