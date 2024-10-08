package beBig.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseVo {
    private String finTypeCode;
    private String userNickname;
    private String badgeCode;
    private int userVisibility;
    private String userIntro;
    private String finTypeInfo;
}
