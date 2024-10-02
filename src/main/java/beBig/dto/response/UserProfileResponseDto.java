package beBig.dto.response;

import beBig.vo.UserRankVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDto {
    private String finTypeCode;
    private String userNickname;
    private String badgeCode;
    private int userVisibility;
    private String userIntro;
    private String finTypeInfo;
    private long userRank;
}

