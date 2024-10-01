package beBig.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @ToString
public class AgeComparisonResponseDto {
    private int age;
    private int ageRange;
    private long TotalAssets;
    private int ageComparisonRank;
}
