package beBig.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString
public class UserTotalAssetsDto {
    private long userId;
    private int age;
    private int ageRange;
    private long totalAssets;
    private int rank;
}
