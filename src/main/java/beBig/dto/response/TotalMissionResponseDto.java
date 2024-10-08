package beBig.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalMissionResponseDto {
    private int restDays;
    private int currentScore;
}
