package beBig.dto.response;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MonthlyMissionResponseDto {
    private int personalMonthlyMissionId;
    private String missionTopic;
    private int missionId;
    private int personalMonthlyMissionCompleted;
    private int missionType;
}
