package beBig.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MissionCompleteRequestDto {
    Long personalMissionId;
    int missionType;
}
