package beBig.dto.response;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString
public class SpendingPatternsResponseDto {
    List<Long> monthlySum;
    long monthlyAverage;
    long previousMonthDiff;

}
