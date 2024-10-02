package beBig.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString
public class UserTotalAssetsDto implements Comparable<UserTotalAssetsDto> {
    private long userId;
    private int age;
    private int ageRange;
    private long totalAssets;
    private int rank;
    private long totalSameAgeRangeUsers;

    @Override
    public int compareTo(UserTotalAssetsDto o) {
        return (int)(o.totalAssets -this.totalAssets);
    }
}
